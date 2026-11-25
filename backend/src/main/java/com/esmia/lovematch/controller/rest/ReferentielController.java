package com.esmia.lovematch.controller.rest;

import com.esmia.lovematch.dto.response.InteretResponse;
import com.esmia.lovematch.dto.response.VilleResponse;
import com.esmia.lovematch.service.ReferentielService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Listes publiques : villes du monde et centres d'intérêt (formulaire d'inscription)
@RestController
@Validated
@RequestMapping("/api/referentiels")
public class ReferentielController {

    private final ReferentielService referentiels;

    public ReferentielController(ReferentielService referentiels) {
        this.referentiels = referentiels;
    }

    // villes du monde, au fil de la frappe : /api/referentiels/villes?q=anta
    @GetMapping("/villes")
    public List<VilleResponse> villes(@RequestParam(defaultValue = "") @Size(max = 60) String q,
                                      @RequestParam(defaultValue = "8") @Min(1) @Max(20) int limite) {
        return referentiels.villes(q, limite);
    }

    @GetMapping("/interets")
    public List<InteretResponse> interets() {
        return referentiels.interets();
    }
}
