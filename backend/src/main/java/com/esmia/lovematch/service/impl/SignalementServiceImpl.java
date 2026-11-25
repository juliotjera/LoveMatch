package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.dto.request.SignalementRequest;
import com.esmia.lovematch.dto.response.SignalementResponse;
import com.esmia.lovematch.dto.response.StatistiquesResponse;
import com.esmia.lovematch.entity.Message;
import com.esmia.lovematch.entity.Signalement;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.Role;
import com.esmia.lovematch.enumeration.StatutCompte;
import com.esmia.lovematch.enumeration.StatutSignalement;
import com.esmia.lovematch.exception.RegleMetierException;
import com.esmia.lovematch.exception.RessourceIntrouvableException;
import com.esmia.lovematch.exception.TropDeDemandesException;
import com.esmia.lovematch.repository.CorrespondanceRepository;
import com.esmia.lovematch.repository.MessageRepository;
import com.esmia.lovematch.repository.SignalementRepository;
import com.esmia.lovematch.repository.UtilisateurRepository;
import com.esmia.lovematch.service.SignalementService;
import com.esmia.lovematch.util.Horloge;
import com.esmia.lovematch.util.LimiteurDebit;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SignalementServiceImpl implements SignalementService {

    private final SignalementRepository signalements;
    private final UtilisateurRepository utilisateurs;
    private final MessageRepository messages;
    private final CorrespondanceRepository correspondances;
    private final LimiteurDebit limiteur;

    public SignalementServiceImpl(SignalementRepository signalements, UtilisateurRepository utilisateurs,
                                  MessageRepository messages, CorrespondanceRepository correspondances, LimiteurDebit limiteur) {
        this.signalements = signalements;
        this.utilisateurs = utilisateurs;
        this.messages = messages;
        this.correspondances = correspondances;
        this.limiteur = limiteur;
    }

    @Override
    public void signaler(Long moi, SignalementRequest demande) {
        if (moi.equals(demande.cibleId())) {
            throw new RegleMetierException("erreur.signaler.soi");
        }
        if (!limiteur.autoriser("signalement:" + moi, 10, Duration.ofDays(1))) {
            throw new TropDeDemandesException("erreur.signalements.trop");
        }
        Utilisateur cible = utilisateurs.findById(demande.cibleId())
            .orElseThrow(() -> new RessourceIntrouvableException("erreur.profil.introuvable"));
        Message message = null;
        if (demande.messageId() != null) {
            // seul un message reçu dans une de ses conversations peut être signalé
            message = messages.findById(demande.messageId())
                .filter(m -> m.getAuteur().getId().equals(cible.getId()) && m.getCorrespondance().concerne(moi))
                .orElseThrow(() -> new RessourceIntrouvableException("erreur.message.introuvable"));
        }
        String commentaire = demande.commentaire() == null || demande.commentaire().isBlank() ? null : demande.commentaire().strip();
        signalements.save(new Signalement(utilisateurs.getReferenceById(moi), cible, message, demande.motif(), commentaire));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SignalementResponse> ouverts() {
        return signalements.parStatut(StatutSignalement.OUVERT).stream().map(this::reponse).toList();
    }

    @Override
    public SignalementResponse decider(Long administrateur, Long signalement, StatutSignalement decision) {
        if (decision == StatutSignalement.OUVERT) {
            throw new RegleMetierException("erreur.decision.invalide");
        }
        Signalement s = signalements.findById(signalement)
            .orElseThrow(() -> new RessourceIntrouvableException("erreur.signalement.introuvable"));
        if (s.getStatut() != StatutSignalement.OUVERT) {
            throw new RegleMetierException("erreur.signalement.traite");
        }
        s.traiter(decision, utilisateurs.getReferenceById(administrateur));
        if (decision == StatutSignalement.SUSPENDU) {
            Utilisateur cible = s.getCible();
            if (cible.getRole() == Role.ADMIN) {
                throw new RegleMetierException("erreur.admin.suspension");
            }
            // le jeton n'est plus accepté (FiltreJeton vérifie le statut) et ses conversations se ferment
            cible.setStatut(StatutCompte.SUSPENDU);
            correspondances.ouvertesDe(cible.getId()).forEach(c -> c.fermer());
        }
        return reponse(s);
    }

    @Override
    public void reactiver(Long membre) {
        Utilisateur u = utilisateurs.findById(membre).orElseThrow(() -> new RessourceIntrouvableException("erreur.compte.introuvable"));
        u.setStatut(StatutCompte.ACTIF);
    }

    @Override
    @Transactional(readOnly = true)
    public StatistiquesResponse statistiques() {
        return new StatistiquesResponse(utilisateurs.countByRole(Role.MEMBRE), utilisateurs.countByStatut(StatutCompte.SUSPENDU),
            correspondances.countByFermeLeIsNull(), messages.countByEnvoyeLeAfter(Horloge.maintenant().minusDays(1)),
            signalements.countByStatut(StatutSignalement.OUVERT));
    }

    private SignalementResponse reponse(Signalement s) {
        Utilisateur cible = s.getCible();
        return new SignalementResponse(s.getId(), s.getMotif(), s.getCommentaire(), s.getStatut(), s.getCreeLe(),
            s.getAuteur().getId(), s.getAuteur().getPrenom(), cible.getId(), cible.getPrenom() + " " + cible.getNom(),
            cible.getEmail(), !cible.estActif(), signalements.countByCibleId(cible.getId()),
            s.getMessage() == null ? null : s.getMessage().getContenu());
    }
}
