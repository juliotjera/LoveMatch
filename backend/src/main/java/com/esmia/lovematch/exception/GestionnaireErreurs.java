package com.esmia.lovematch.exception;

import com.esmia.lovematch.dto.response.ErreurResponse;
import com.esmia.lovematch.util.Horloge;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

// Toutes les erreurs de l'API au même format : statut, code, message lisible, champs en erreur
@RestControllerAdvice
public class GestionnaireErreurs {

    private final MessageSource messages;

    public GestionnaireErreurs(MessageSource messages) {
        this.messages = messages;
    }

    private static final Logger journal = LoggerFactory.getLogger(GestionnaireErreurs.class);

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurResponse> introuvable(RessourceIntrouvableException e) {
        return reponse(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(RegleMetierException.class)
    public ResponseEntity<ErreurResponse> regle(RegleMetierException e) {
        return reponse(HttpStatus.CONFLICT, e.getMessage(), null);
    }

    @ExceptionHandler(AccesInterditException.class)
    public ResponseEntity<ErreurResponse> interdit(AccesInterditException e) {
        return reponse(HttpStatus.FORBIDDEN, e.getMessage(), null);
    }

    @ExceptionHandler(TropDeDemandesException.class)
    public ResponseEntity<ErreurResponse> tropDeDemandes(TropDeDemandesException e) {
        return reponse(HttpStatus.TOO_MANY_REQUESTS, e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurResponse> validation(MethodArgumentNotValidException e) {
        Map<String, String> champs = new LinkedHashMap<>();
        for (FieldError erreur : e.getBindingResult().getFieldErrors()) {
            champs.putIfAbsent(erreur.getField(), erreur.getDefaultMessage());
        }
        return reponse(HttpStatus.BAD_REQUEST, "erreur.champs", champs);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErreurResponse> illisible(Exception e) {
        return reponse(HttpStatus.BAD_REQUEST, "erreur.requete", null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErreurResponse> tropLourd(MaxUploadSizeExceededException e) {
        return reponse(HttpStatus.PAYLOAD_TOO_LARGE, "erreur.photo.lourde", null);
    }

    // deux requêtes simultanées sur une contrainte unique (double like, double blocage…)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurResponse> conflit(DataIntegrityViolationException e) {
        return reponse(HttpStatus.CONFLICT, "erreur.deja.enregistre", null);
    }

    // adresse inconnue : JSON pour l'API et les fichiers ; pour une page (lien ancien, faute de frappe), l'interface
    // s'ouvre avec son propre écran « Page introuvable », traduit, et le code HTTP 404
    @ExceptionHandler(NoResourceFoundException.class)
    public Object adresseInconnue(NoResourceFoundException e, HttpServletRequest requete) {
        String chemin = requete.getRequestURI();
        String dernier = chemin.substring(chemin.lastIndexOf('/') + 1);
        boolean page = "GET".equals(requete.getMethod()) && !chemin.startsWith("/api/") && !chemin.startsWith("/ws")
            && !chemin.startsWith("/actuator") && !dernier.contains(".");
        if (page) {
            ModelAndView interfaceAngular = new ModelAndView("forward:/index.html");
            interfaceAngular.setStatus(HttpStatus.NOT_FOUND);
            return interfaceAngular;
        }
        return reponse(HttpStatus.NOT_FOUND, "erreur.adresse", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurResponse> imprevue(Exception e) {
        journal.error("Erreur imprévue", e);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "erreur.imprevue", null);
    }

    // « message » est une clé de messages*.properties, traduite dans la langue de la requête
    private ResponseEntity<ErreurResponse> reponse(HttpStatus statut, String message, Map<String, String> champs) {
        String texte = messages.getMessage(message, null, message, LocaleContextHolder.getLocale());
        return ResponseEntity.status(statut)
            .body(new ErreurResponse(statut.value(), statut.name(), texte, champs, Horloge.maintenant()));
    }
}
