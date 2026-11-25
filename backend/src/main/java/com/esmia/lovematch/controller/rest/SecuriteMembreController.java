package com.esmia.lovematch.controller.rest;

import com.esmia.lovematch.dto.request.SignalementRequest;
import com.esmia.lovematch.dto.response.ProfilPublicResponse;
import com.esmia.lovematch.security.SessionCourante;
import com.esmia.lovematch.service.BlocageService;
import com.esmia.lovematch.service.SignalementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Blocage et signalement
@RestController
@RequestMapping("/api")
public class SecuriteMembreController {

    private final BlocageService blocages;
    private final SignalementService signalements;
    private final SessionCourante session;

    public SecuriteMembreController(BlocageService blocages, SignalementService signalements, SessionCourante session) {
        this.blocages = blocages;
        this.signalements = signalements;
        this.session = session;
    }

    @GetMapping("/blocages")
    public List<ProfilPublicResponse> bloques() {
        return blocages.bloques(session.id());
    }

    @PostMapping("/blocages/{cibleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bloquer(@PathVariable Long cibleId) {
        blocages.bloquer(session.id(), cibleId);
    }

    @DeleteMapping("/blocages/{cibleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void debloquer(@PathVariable Long cibleId) {
        blocages.debloquer(session.id(), cibleId);
    }

    @PostMapping("/signalements")
    @ResponseStatus(HttpStatus.CREATED)
    public void signaler(@Valid @RequestBody SignalementRequest demande) {
        signalements.signaler(session.id(), demande);
    }
}
