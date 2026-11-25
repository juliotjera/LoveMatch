package com.esmia.lovematch.controller.rest;

import com.esmia.lovematch.dto.request.DecisionSignalementRequest;
import com.esmia.lovematch.dto.response.SignalementResponse;
import com.esmia.lovematch.dto.response.StatistiquesResponse;
import com.esmia.lovematch.security.SessionCourante;
import com.esmia.lovematch.service.SignalementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Modération (rôle ADMIN, vérifié par la configuration de sécurité)
@RestController
@RequestMapping("/api/admin")
public class AdministrationController {

    private final SignalementService signalements;
    private final SessionCourante session;

    public AdministrationController(SignalementService signalements, SessionCourante session) {
        this.signalements = signalements;
        this.session = session;
    }

    @GetMapping("/statistiques")
    public StatistiquesResponse statistiques() {
        return signalements.statistiques();
    }

    @GetMapping("/signalements")
    public List<SignalementResponse> ouverts() {
        return signalements.ouverts();
    }

    @PostMapping("/signalements/{id}/decision")
    public SignalementResponse decider(@PathVariable Long id, @Valid @RequestBody DecisionSignalementRequest demande) {
        return signalements.decider(session.id(), id, demande.decision());
    }

    @PostMapping("/membres/{id}/reactivation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactiver(@PathVariable Long id) {
        signalements.reactiver(id);
    }
}
