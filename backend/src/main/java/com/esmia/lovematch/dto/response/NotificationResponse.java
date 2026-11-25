package com.esmia.lovematch.dto.response;

import com.esmia.lovematch.enumeration.TypeNotification;
import java.time.LocalDateTime;

// prenom : la personne concernée (match, message) ; toujours vide pour un like, qui reste anonyme
public record NotificationResponse(Long id, TypeNotification type, Long correspondanceId, String prenom,
                                   boolean lue, LocalDateTime creeLe) {}
