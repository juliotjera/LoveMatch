package com.esmia.lovematch.dto.request;

import com.esmia.lovematch.enumeration.Langue;
import com.esmia.lovematch.enumeration.Recherche;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProfilRequest(
    @NotBlank @Size(max = 60) String prenom,
    @NotBlank @Size(max = 60) String nom,
    @NotNull Long villeId,
    @Size(max = 500) String bio,
    @NotNull Recherche recherche,
    @NotNull @Size(min = 3, max = 8, message = "{interets.nombre}") List<Long> interets,
    @NotNull @Size(min = 1, max = 5, message = "{langues.nombre}") List<Langue> langues
) {}
