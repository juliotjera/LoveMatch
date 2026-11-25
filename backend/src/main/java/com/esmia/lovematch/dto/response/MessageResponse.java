package com.esmia.lovematch.dto.response;

import java.time.LocalDateTime;

public record MessageResponse(Long id, Long correspondanceId, Long auteurId, String contenu,
                              LocalDateTime envoyeLe, LocalDateTime luLe) {}
