package com.esmia.lovematch.service;

import com.esmia.lovematch.dto.request.ReactionRequest;
import com.esmia.lovematch.dto.response.ReactionResponse;

public interface ReactionService {

    ReactionResponse reagir(Long moi, ReactionRequest demande);

    void annulerPasse(Long moi, Long cible);

    long likesRecus(Long moi);
}
