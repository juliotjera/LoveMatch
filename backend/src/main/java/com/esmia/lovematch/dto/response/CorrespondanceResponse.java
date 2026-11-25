package com.esmia.lovematch.dto.response;

import java.time.LocalDateTime;

public record CorrespondanceResponse(Long id, ProfilPublicResponse autre, LocalDateTime creeLe, boolean ouverte,
                                     MessageResponse dernierMessage, long nonLus) {}
