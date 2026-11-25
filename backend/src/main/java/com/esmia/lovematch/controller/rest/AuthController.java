package com.esmia.lovematch.controller.rest;

import com.esmia.lovematch.dto.request.ConnexionRequest;
import com.esmia.lovematch.dto.request.InscriptionRequest;
import com.esmia.lovematch.dto.response.MoiResponse;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.Role;
import com.esmia.lovematch.exception.RessourceIntrouvableException;
import com.esmia.lovematch.security.CookieSession;
import com.esmia.lovematch.security.JetonService;
import com.esmia.lovematch.service.AuthService;
import com.esmia.lovematch.service.ProfilService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final ProfilService profils;
    private final JetonService jetons;
    private final CookieSession cookie;

    public AuthController(AuthService auth, ProfilService profils, JetonService jetons, CookieSession cookie) {
        this.auth = auth;
        this.profils = profils;
        this.jetons = jetons;
        this.cookie = cookie;
    }

    @PostMapping("/inscription")
    public ResponseEntity<MoiResponse> inscrire(@Valid @RequestBody InscriptionRequest demande) {
        return session(auth.inscrire(demande), HttpStatus.CREATED);
    }

    @PostMapping("/connexion")
    public ResponseEntity<MoiResponse> connecter(@Valid @RequestBody ConnexionRequest demande, HttpServletRequest requete) {
        return session(auth.connecter(demande, requete.getRemoteAddr()), HttpStatus.OK);
    }

    @PostMapping("/deconnexion")
    public ResponseEntity<Void> deconnecter() {
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.effacer().toString()).build();
    }

    // l'interface affiche les boutons « Essayer sans compte » seulement si la démonstration est activée
    @GetMapping("/demo")
    public Map<String, Boolean> demo() {
        return Map.of("active", auth.demoActive());
    }

    @PostMapping("/demo/{role}")
    public ResponseEntity<MoiResponse> essayer(@PathVariable String role) {
        Role choisi = switch (role) {
            case "membre" -> Role.MEMBRE;
            case "admin" -> Role.ADMIN;
            default -> throw new RessourceIntrouvableException("erreur.demo.role");
        };
        return session(auth.compteDemo(choisi), HttpStatus.OK);
    }

    private ResponseEntity<MoiResponse> session(Utilisateur u, HttpStatus statut) {
        return ResponseEntity.status(statut)
            .header(HttpHeaders.SET_COOKIE, cookie.creer(jetons.creer(u), jetons.duree()).toString())
            .body(profils.moi(u.getId()));
    }
}
