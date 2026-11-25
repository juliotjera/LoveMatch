package com.esmia.lovematch.service;

import com.esmia.lovematch.dto.response.InteretResponse;
import com.esmia.lovematch.dto.response.VilleResponse;
import java.util.List;

public interface ReferentielService {

    // villes dont le nom commence par « debut » (accents et majuscules ignorés)
    List<VilleResponse> villes(String debut, int limite);

    List<InteretResponse> interets();
}
