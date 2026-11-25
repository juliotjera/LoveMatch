package com.esmia.lovematch.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

// Format uniforme des erreurs de l'API
public record ErreurResponse(int statut, String erreur, String message, Map<String, String> champs, LocalDateTime horodatage) {}
