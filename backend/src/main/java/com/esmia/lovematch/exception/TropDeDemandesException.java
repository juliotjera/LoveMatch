package com.esmia.lovematch.exception;

// Limite de débit atteinte (likes, messages, connexions) : 429
public class TropDeDemandesException extends RuntimeException {

    public TropDeDemandesException(String message) {
        super(message);
    }
}
