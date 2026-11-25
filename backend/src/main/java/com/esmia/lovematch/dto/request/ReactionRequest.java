package com.esmia.lovematch.dto.request;

import com.esmia.lovematch.enumeration.Decision;
import jakarta.validation.constraints.NotNull;

public record ReactionRequest(@NotNull Long cibleId, @NotNull Decision decision) {}
