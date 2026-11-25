package com.esmia.lovematch.dto.request;

import com.esmia.lovematch.enumeration.GenreRecherche;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PreferencesRequest(
    @NotNull GenreRecherche genreRecherche,
    @Min(18) @Max(99) int ageMin,
    @Min(18) @Max(99) int ageMax,
    @Min(10) @Max(1000) int rayonKm
) {}
