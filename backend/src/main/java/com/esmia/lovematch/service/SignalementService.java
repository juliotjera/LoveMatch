package com.esmia.lovematch.service;

import com.esmia.lovematch.dto.request.SignalementRequest;
import com.esmia.lovematch.dto.response.SignalementResponse;
import com.esmia.lovematch.dto.response.StatistiquesResponse;
import com.esmia.lovematch.enumeration.StatutSignalement;
import java.util.List;

public interface SignalementService {

    void signaler(Long moi, SignalementRequest demande);

    List<SignalementResponse> ouverts();

    SignalementResponse decider(Long administrateur, Long signalement, StatutSignalement decision);

    void reactiver(Long membre);

    StatistiquesResponse statistiques();
}
