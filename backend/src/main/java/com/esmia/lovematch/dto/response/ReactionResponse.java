package com.esmia.lovematch.dto.response;

// match = true quand le like est réciproque : la conversation est alors ouverte
public record ReactionResponse(boolean match, Long correspondanceId) {}
