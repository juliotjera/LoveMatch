package com.esmia.lovematch.service;

import com.esmia.lovematch.dto.request.PreferencesRequest;
import com.esmia.lovematch.dto.request.ProfilRequest;
import com.esmia.lovematch.dto.response.MoiResponse;
import com.esmia.lovematch.dto.response.ProfilPublicResponse;

public interface ProfilService {

    MoiResponse moi(Long moi);

    MoiResponse modifierProfil(Long moi, ProfilRequest demande);

    MoiResponse modifierPreferences(Long moi, PreferencesRequest demande);

    ProfilPublicResponse profilPublic(Long moi, Long membre);

    void supprimerCompte(Long moi);
}
