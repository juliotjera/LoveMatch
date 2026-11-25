package com.esmia.lovematch.controller.rest;

import com.esmia.lovematch.dto.request.ReactionRequest;
import com.esmia.lovematch.dto.response.PageResponse;
import com.esmia.lovematch.dto.response.ReactionResponse;
import com.esmia.lovematch.dto.response.SuggestionResponse;
import com.esmia.lovematch.security.SessionCourante;
import com.esmia.lovematch.service.ReactionService;
import com.esmia.lovematch.service.SuggestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Map;
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

// Suggestions triées par score, et réactions (like, passer)
@RestController
@RequestMapping("/api")
@Validated
public class DecouverteController {

    private final SuggestionService suggestions;
    private final ReactionService reactions;
    private final SessionCourante session;

    public DecouverteController(SuggestionService suggestions, ReactionService reactions, SessionCourante session) {
        this.suggestions = suggestions;
        this.reactions = reactions;
        this.session = session;
    }

    @GetMapping("/suggestions")
    public PageResponse<SuggestionResponse> suggestions(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                        @RequestParam(defaultValue = "12") @Min(1) @Max(50) int taille,
                                                        @RequestParam(required = false) @Min(1) Integer distanceMax,
                                                        @RequestParam(defaultValue = "false") boolean interetsCommuns) {
        return suggestions.suggestions(session.id(), page, taille, distanceMax, interetsCommuns);
    }

    @PostMapping("/reactions")
    @ResponseStatus(HttpStatus.CREATED)
    public ReactionResponse reagir(@Valid @RequestBody ReactionRequest demande) {
        return reactions.reagir(session.id(), demande);
    }

    @DeleteMapping("/reactions/passes/{cibleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void annulerPasse(@PathVariable Long cibleId) {
        reactions.annulerPasse(session.id(), cibleId);
    }

    // « N personnes vous ont liké » : un nombre, jamais des noms
    @GetMapping("/reactions/recues")
    public Map<String, Long> likesRecus() {
        return Map.of("nombre", reactions.likesRecus(session.id()));
    }
}
