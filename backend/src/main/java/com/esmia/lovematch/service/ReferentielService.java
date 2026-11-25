package com.esmia.lovematch.service;

import com.esmia.lovematch.dto.response.InteretResponse;
import com.esmia.lovematch.dto.response.VilleResponse;
import java.util.List;

public interface ReferentielService {

    List<VilleResponse> villes();

    List<InteretResponse> interets();
}
