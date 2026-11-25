package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.dto.request.PreferencesRequest;
import com.esmia.lovematch.dto.request.ProfilRequest;
import com.esmia.lovematch.dto.response.MoiResponse;
import com.esmia.lovematch.dto.response.ProfilPublicResponse;
import com.esmia.lovematch.entity.Interet;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.Role;
import com.esmia.lovematch.exception.RegleMetierException;
import com.esmia.lovematch.exception.RessourceIntrouvableException;
import com.esmia.lovematch.mapper.ProfilMapper;
import com.esmia.lovematch.repository.BlocageRepository;
import com.esmia.lovematch.repository.InteretRepository;
import com.esmia.lovematch.repository.UtilisateurRepository;
import com.esmia.lovematch.repository.VilleRepository;
import com.esmia.lovematch.service.ProfilService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfilServiceImpl implements ProfilService {

    private final UtilisateurRepository utilisateurs;
    private final VilleRepository villes;
    private final InteretRepository interets;
    private final BlocageRepository blocages;
    private final ProfilMapper mapper;

    public ProfilServiceImpl(UtilisateurRepository utilisateurs, VilleRepository villes, InteretRepository interets,
                             BlocageRepository blocages, ProfilMapper mapper) {
        this.utilisateurs = utilisateurs;
        this.villes = villes;
        this.interets = interets;
        this.blocages = blocages;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public MoiResponse moi(Long moi) {
        return mapper.moi(charger(moi));
    }

    @Override
    public MoiResponse modifierProfil(Long moi, ProfilRequest demande) {
        Utilisateur u = charger(moi);
        List<Interet> choisis = interets.findAllById(demande.interets());
        if (choisis.size() != demande.interets().stream().distinct().count()) {
            throw new RessourceIntrouvableException("erreur.interet.inconnu");
        }
        u.setPrenom(demande.prenom().trim());
        u.setNom(demande.nom().trim());
        u.setVille(villes.findById(demande.villeId()).orElseThrow(() -> new RessourceIntrouvableException("erreur.ville.inconnue")));
        u.setBio(demande.bio() == null || demande.bio().isBlank() ? null : demande.bio().trim());
        u.setRecherche(demande.recherche());
        u.getInterets().clear();
        u.getInterets().addAll(choisis);
        u.getLangues().clear();
        u.getLangues().addAll(demande.langues());
        return mapper.moi(u);
    }

    @Override
    public MoiResponse modifierPreferences(Long moi, PreferencesRequest demande) {
        if (demande.ageMin() > demande.ageMax()) {
            throw new RegleMetierException("erreur.tranche");
        }
        Utilisateur u = charger(moi);
        u.setGenreRecherche(demande.genreRecherche());
        u.setAgeMin(demande.ageMin());
        u.setAgeMax(demande.ageMax());
        u.setRayonKm(demande.rayonKm());
        return mapper.moi(u);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfilPublicResponse profilPublic(Long moi, Long membre) {
        Utilisateur u = utilisateurs.findById(membre)
            .filter(trouve -> trouve.getRole() == Role.MEMBRE && trouve.estActif())
            .filter(trouve -> moi.equals(trouve.getId()) || !blocages.entre(moi, trouve.getId()))
            .orElseThrow(() -> new RessourceIntrouvableException("erreur.profil.introuvable"));
        return mapper.publicDe(u);
    }

    @Override
    public void supprimerCompte(Long moi) {
        // photos, réactions, matchs, messages, notifications et blocages partent avec le compte (on delete cascade)
        utilisateurs.delete(charger(moi));
    }

    private Utilisateur charger(Long id) {
        return utilisateurs.findById(id).orElseThrow(() -> new RessourceIntrouvableException("erreur.compte.introuvable"));
    }
}
