package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.dto.request.ConnexionRequest;
import com.esmia.lovematch.dto.request.InscriptionRequest;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.entity.Ville;
import com.esmia.lovematch.enumeration.Langue;
import com.esmia.lovematch.enumeration.Role;
import com.esmia.lovematch.exception.AccesInterditException;
import com.esmia.lovematch.exception.RegleMetierException;
import com.esmia.lovematch.exception.RessourceIntrouvableException;
import com.esmia.lovematch.exception.TropDeDemandesException;
import com.esmia.lovematch.repository.UtilisateurRepository;
import com.esmia.lovematch.repository.VilleRepository;
import com.esmia.lovematch.service.AuthService;
import com.esmia.lovematch.util.Horloge;
import com.esmia.lovematch.util.LimiteurDebit;
import java.time.Duration;
import java.time.Period;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    public static final int AGE_MINIMUM = 18;
    // même message que le compte existe ou non : on ne révèle pas qui est inscrit
    private static final String IDENTIFIANTS_INCORRECTS = "erreur.identifiants";

    private final UtilisateurRepository utilisateurs;
    private final VilleRepository villes;
    private final PasswordEncoder encodeur;
    private final LimiteurDebit limiteur;
    private final boolean demo;

    public AuthServiceImpl(UtilisateurRepository utilisateurs, VilleRepository villes, PasswordEncoder encodeur,
                           LimiteurDebit limiteur, @Value("${lovematch.demo}") boolean demo) {
        this.utilisateurs = utilisateurs;
        this.villes = villes;
        this.encodeur = encodeur;
        this.limiteur = limiteur;
        this.demo = demo;
    }

    @Override
    public Utilisateur inscrire(InscriptionRequest demande) {
        int age = Period.between(demande.dateNaissance(), Horloge.aujourdhui()).getYears();
        if (age < AGE_MINIMUM) {
            throw new RegleMetierException("erreur.majeur");
        }
        String email = demande.email().trim().toLowerCase(Locale.ROOT);
        if (utilisateurs.existsByEmailIgnoreCase(email)) {
            throw new RegleMetierException("erreur.email.pris");
        }
        Ville ville = villes.findById(demande.villeId()).orElseThrow(() -> new RessourceIntrouvableException("erreur.ville.inconnue"));

        Utilisateur u = new Utilisateur();
        u.setEmail(email);
        u.setMotDePasse(encodeur.encode(demande.motDePasse()));
        u.setPrenom(demande.prenom().trim());
        u.setNom(demande.nom().trim());
        u.setDateNaissance(demande.dateNaissance());
        u.setGenre(demande.genre());
        u.setVille(ville);
        u.setGenreRecherche(demande.genreRecherche());
        // préférences proposées : cinq ans de part et d'autre, 200 km ; modifiables ensuite
        u.setAgeMin(Math.max(AGE_MINIMUM, age - 5));
        u.setAgeMax(Math.min(99, age + 5));
        u.setRayonKm(200);
        if (demande.langues() == null || demande.langues().isEmpty()) {
            u.getLangues().add(Langue.FR);
        } else {
            u.getLangues().addAll(demande.langues());
        }
        return utilisateurs.save(u);
    }

    @Override
    public Utilisateur connecter(ConnexionRequest demande, String adresseIp) {
        if (!limiteur.autoriser("connexion:" + adresseIp, 10, Duration.ofMinutes(10))) {
            throw new TropDeDemandesException("erreur.connexion.trop");
        }
        Utilisateur u = utilisateurs.findByEmailIgnoreCase(demande.email().trim())
            .filter(trouve -> encodeur.matches(demande.motDePasse(), trouve.getMotDePasse()))
            .orElseThrow(() -> new RegleMetierException(IDENTIFIANTS_INCORRECTS));
        if (!u.estActif()) {
            throw new AccesInterditException("erreur.compte.suspendu");
        }
        u.setDerniereActivite(Horloge.maintenant());
        return u;
    }

    @Override
    public Utilisateur compteDemo(Role role) {
        if (!demo) {
            throw new RessourceIntrouvableException("erreur.demo.inactive");
        }
        String email = role == Role.ADMIN ? DemoServiceImpl.EMAIL_MODERATION : DemoServiceImpl.EMAIL_MEMBRE;
        Utilisateur u = utilisateurs.findByEmailIgnoreCase(email)
            .filter(Utilisateur::isDemo)
            .orElseThrow(() -> new RessourceIntrouvableException("erreur.demo.compte"));
        u.setDerniereActivite(Horloge.maintenant());
        return u;
    }

    @Override
    public boolean demoActive() {
        return demo;
    }
}
