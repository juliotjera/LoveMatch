package com.esmia.lovematch.service;

import com.esmia.lovematch.dto.request.ConnexionRequest;
import com.esmia.lovematch.dto.request.InscriptionRequest;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.Role;

public interface AuthService {

    Utilisateur inscrire(InscriptionRequest demande);

    Utilisateur connecter(ConnexionRequest demande, String adresseIp);

    // compte de démonstration (« Essayer sans compte »), seulement si la démonstration est activée
    Utilisateur compteDemo(Role role);

    boolean demoActive();
}
