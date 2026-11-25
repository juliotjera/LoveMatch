package com.esmia.lovematch.dto.response;

import com.esmia.lovematch.enumeration.Genre;
import com.esmia.lovematch.enumeration.Langue;
import com.esmia.lovematch.enumeration.Recherche;
import java.util.List;

// Ce que les autres membres voient : ni nom de famille, ni e-mail, ni date de naissance exacte
public record ProfilPublicResponse(
    Long id, String prenom, int age, Genre genre, String ville, String region, String bio,
    Recherche recherche, List<InteretResponse> interets, List<Langue> langues, List<Long> photos
) {}
