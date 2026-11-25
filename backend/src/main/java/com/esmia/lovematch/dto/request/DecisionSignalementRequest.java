package com.esmia.lovematch.dto.request;

import com.esmia.lovematch.enumeration.StatutSignalement;
import jakarta.validation.constraints.NotNull;

public record DecisionSignalementRequest(@NotNull StatutSignalement decision) {}
