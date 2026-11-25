package com.esmia.lovematch.controller.rest;

import com.esmia.lovematch.dto.request.PreferencesRequest;
import com.esmia.lovematch.dto.request.ProfilRequest;
import com.esmia.lovematch.dto.response.MoiResponse;
import com.esmia.lovematch.dto.response.ProfilPublicResponse;
import com.esmia.lovematch.entity.Photo;
import com.esmia.lovematch.security.CookieSession;
import com.esmia.lovematch.security.SessionCourante;
import com.esmia.lovematch.service.PhotoService;
import com.esmia.lovematch.service.ProfilService;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ProfilController {

    private final ProfilService profils;
    private final PhotoService photos;
    private final SessionCourante session;
    private final CookieSession cookie;

    public ProfilController(ProfilService profils, PhotoService photos, SessionCourante session, CookieSession cookie) {
        this.profils = profils;
        this.photos = photos;
        this.session = session;
        this.cookie = cookie;
    }

    @GetMapping("/profil")
    public MoiResponse moi() {
        return profils.moi(session.id());
    }

    @PutMapping("/profil")
    public MoiResponse modifier(@Valid @RequestBody ProfilRequest demande) {
        return profils.modifierProfil(session.id(), demande);
    }

    @PutMapping("/profil/preferences")
    public MoiResponse preferences(@Valid @RequestBody PreferencesRequest demande) {
        return profils.modifierPreferences(session.id(), demande);
    }

    // droit à l'effacement : le compte et toutes ses données
    @DeleteMapping("/profil")
    public ResponseEntity<Void> supprimer() {
        profils.supprimerCompte(session.id());
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.effacer().toString()).build();
    }

    @PostMapping(path = "/profil/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> ajouterPhoto(@RequestParam("fichier") MultipartFile fichier) {
        return Map.of("id", photos.ajouter(session.id(), fichier));
    }

    @DeleteMapping("/profil/photos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerPhoto(@PathVariable Long id) {
        photos.supprimer(session.id(), id);
    }

    @PutMapping("/profil/photos/{id}/principale")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void photoPrincipale(@PathVariable Long id) {
        photos.definirPrincipale(session.id(), id);
    }

    @GetMapping("/photos/{id}")
    public ResponseEntity<byte[]> photo(@PathVariable Long id) {
        Photo p = photos.lire(session.id(), id);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(p.getTypeMime()))
            .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePrivate())
            .body(p.getContenu());
    }

    @GetMapping("/membres/{id}")
    public ProfilPublicResponse membre(@PathVariable Long id) {
        return profils.profilPublic(session.id(), id);
    }
}
