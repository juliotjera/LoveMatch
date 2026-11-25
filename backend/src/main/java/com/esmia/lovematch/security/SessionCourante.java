package com.esmia.lovematch.security;

import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.exception.RessourceIntrouvableException;
import com.esmia.lovematch.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// Le membre de la requête en cours (déposé par FiltreJeton)
@Component
public class SessionCourante {

    private final UtilisateurRepository utilisateurs;

    public SessionCourante(UtilisateurRepository utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    public Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof MembreConnecte membre)) {
            throw new IllegalStateException("Aucun membre connecté");
        }
        return membre.id();
    }

    public Utilisateur utilisateur() {
        return utilisateurs.findById(id()).orElseThrow(() -> new RessourceIntrouvableException("erreur.compte.introuvable"));
    }
}
