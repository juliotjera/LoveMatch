package com.esmia.lovematch.controller.rest;

import com.esmia.lovematch.dto.response.InteretResponse;
import com.esmia.lovematch.dto.response.VilleResponse;
import com.esmia.lovematch.service.ReferentielService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Listes publiques : villes de Madagascar et centres d'intérêt (formulaire d'inscription)
@RestController
@RequestMapping("/api/referentiels")
public class ReferentielController {

    private final ReferentielService referentiels;

    public ReferentielController(ReferentielService referentiels) {
        this.referentiels = referentiels;
    }

    @GetMapping("/villes")
    public List<VilleResponse> villes() {
        return referentiels.villes();
    }

    @GetMapping("/interets")
    public List<InteretResponse> interets() {
        return referentiels.interets();
    }
}
