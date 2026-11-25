package com.esmia.lovematch.exception;

// Règle de gestion non respectée (déjà liké, conversation fermée, profil incomplet…) : 409
public class RegleMetierException extends RuntimeException {

    public RegleMetierException(String message) {
        super(message);
    }
}
