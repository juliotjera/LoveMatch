package com.esmia.lovematch.dto.request;

import com.esmia.lovematch.enumeration.MotifSignalement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignalementRequest(
    @NotNull Long cibleId,
    Long messageId,
    @NotNull MotifSignalement motif,
    @Size(max = 500) String commentaire
) {}
