package com.esmia.lovematch.controller.rest;

import com.esmia.lovematch.dto.request.MessageRequest;
import com.esmia.lovematch.dto.response.CorrespondanceResponse;
import com.esmia.lovematch.dto.response.MessageResponse;
import com.esmia.lovematch.security.SessionCourante;
import com.esmia.lovematch.service.CorrespondanceService;
import com.esmia.lovematch.service.MessagerieService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Matchs et conversations
@RestController
@RequestMapping("/api/matchs")
@Validated
public class MessagerieController {

    private final CorrespondanceService correspondances;
    private final MessagerieService messagerie;
    private final SessionCourante session;

    public MessagerieController(CorrespondanceService correspondances, MessagerieService messagerie, SessionCourante session) {
        this.correspondances = correspondances;
        this.messagerie = messagerie;
        this.session = session;
    }

    @GetMapping
    public List<CorrespondanceResponse> mesMatchs() {
        return correspondances.mesMatchs(session.id());
    }

    @GetMapping("/{id}")
    public CorrespondanceResponse unMatch(@PathVariable Long id) {
        return correspondances.unMatch(session.id(), id);
    }

    // « Ne plus être en contact » : la conversation se ferme pour les deux
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void fermer(@PathVariable Long id) {
        correspondances.fermer(session.id(), id);
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> historique(@PathVariable Long id,
                                            @RequestParam(required = false) Long avant,
                                            @RequestParam(defaultValue = "30") @Min(1) @Max(100) int taille) {
        return messagerie.historique(session.id(), id, avant, taille);
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse envoyer(@PathVariable Long id, @Valid @RequestBody MessageRequest demande) {
        return messagerie.envoyer(session.id(), id, demande.contenu());
    }
}
