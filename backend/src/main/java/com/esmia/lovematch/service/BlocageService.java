package com.esmia.lovematch.service;

import com.esmia.lovematch.dto.response.ProfilPublicResponse;
import java.util.List;

public interface BlocageService {

    void bloquer(Long moi, Long cible);

    void debloquer(Long moi, Long cible);

    List<ProfilPublicResponse> bloques(Long moi);
}
