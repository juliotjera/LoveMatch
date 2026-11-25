package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.dto.request.ReactionRequest;
import com.esmia.lovematch.dto.response.ReactionResponse;
import com.esmia.lovematch.entity.Correspondance;
import com.esmia.lovematch.entity.Reaction;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.Decision;
import com.esmia.lovematch.enumeration.Role;
import com.esmia.lovematch.enumeration.TypeNotification;
import com.esmia.lovematch.exception.RegleMetierException;
import com.esmia.lovematch.exception.RessourceIntrouvableException;
import com.esmia.lovematch.exception.TropDeDemandesException;
import com.esmia.lovematch.repository.BlocageRepository;
import com.esmia.lovematch.repository.CorrespondanceRepository;
import com.esmia.lovematch.repository.ReactionRepository;
import com.esmia.lovematch.repository.UtilisateurRepository;
import com.esmia.lovematch.service.NotificationService;
import com.esmia.lovematch.service.ReactionService;
import com.esmia.lovematch.util.CalculScore;
import com.esmia.lovematch.util.Horloge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Like ou « passer ». Un like n'est révélé à la personne que de façon anonyme ; s'il est réciproque,
 * c'est un match : la conversation s'ouvre et les deux sont prévenus.
 */
@Service
@Transactional
public class ReactionServiceImpl implements ReactionService {

    public static final int LIKES_PAR_JOUR = 100;

    private final ReactionRepository reactions;
    private final CorrespondanceRepository correspondances;
    private final UtilisateurRepository utilisateurs;
    private final BlocageRepository blocages;
    private final NotificationService notifications;

    public ReactionServiceImpl(ReactionRepository reactions, CorrespondanceRepository correspondances,
                               UtilisateurRepository utilisateurs, BlocageRepository blocages,
                               NotificationService notifications) {
        this.reactions = reactions;
        this.correspondances = correspondances;
        this.utilisateurs = utilisateurs;
        this.blocages = blocages;
        this.notifications = notifications;
    }

    @Override
    public ReactionResponse reagir(Long moi, ReactionRequest demande) {
        if (moi.equals(demande.cibleId())) {
            throw new RegleMetierException("erreur.reagir.soi");
        }
        Utilisateur auteur = utilisateurs.findById(moi).orElseThrow(() -> new RessourceIntrouvableException("erreur.compte.introuvable"));
        Utilisateur cible = utilisateurs.findById(demande.cibleId())
            .filter(c -> c.getRole() == Role.MEMBRE && c.estActif() && !blocages.entre(moi, c.getId()))
            .orElseThrow(() -> new RessourceIntrouvableException("erreur.profil.introuvable"));
        // même règle que les suggestions : impossible de liker quelqu'un dont les préférences vous excluent
        if (!CalculScore.compatibles(auteur, cible)) {
            throw new RegleMetierException("erreur.profil.incompatible");
        }
        if (reactions.findByAuteurIdAndCibleId(moi, cible.getId()).isPresent()) {
            throw new RegleMetierException("erreur.reaction.deja");
        }
        if (demande.decision() == Decision.LIKE
                && reactions.countByAuteurIdAndDecisionAndCreeLeAfter(moi, Decision.LIKE, Horloge.maintenant().minusDays(1)) >= LIKES_PAR_JOUR) {
            throw new TropDeDemandesException("erreur.likes.limite");
        }
        reactions.save(new Reaction(auteur, cible, demande.decision()));

        if (demande.decision() == Decision.PASSE) {
            return new ReactionResponse(false, null);
        }
        if (!reactions.existsByAuteurIdAndCibleIdAndDecision(cible.getId(), moi, Decision.LIKE)) {
            notifications.notifier(cible, TypeNotification.LIKE, null);
            return new ReactionResponse(false, null);
        }
        Long a = Math.min(moi, cible.getId());
        Long b = Math.max(moi, cible.getId());
        Correspondance match = correspondances.paire(a, b).orElseGet(() -> correspondances.save(new Correspondance(auteur, cible)));
        notifications.notifier(auteur, TypeNotification.MATCH, match);
        notifications.notifier(cible, TypeNotification.MATCH, match);
        return new ReactionResponse(true, match.getId());
    }

    @Override
    public void annulerPasse(Long moi, Long cible) {
        Reaction r = reactions.findByAuteurIdAndCibleId(moi, cible)
            .filter(trouvee -> trouvee.getDecision() == Decision.PASSE)
            .orElseThrow(() -> new RessourceIntrouvableException("erreur.passe.aucun"));
        reactions.delete(r);
    }

    @Override
    @Transactional(readOnly = true)
    public long likesRecus(Long moi) {
        return reactions.likesEnAttente(moi);
    }
}
