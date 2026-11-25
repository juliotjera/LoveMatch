package com.esmia.lovematch.service;

import com.esmia.lovematch.dto.response.CorrespondanceResponse;
import com.esmia.lovematch.entity.Correspondance;
import java.util.List;

public interface CorrespondanceService {

    List<CorrespondanceResponse> mesMatchs(Long moi);

    CorrespondanceResponse unMatch(Long moi, Long correspondance);

    void fermer(Long moi, Long correspondance);

    // correspondance dont le membre fait partie, sinon 404 (on ne révèle pas l'existence d'une conversation)
    Correspondance acces(Long moi, Long correspondance);
}
