package com.esmia.lovematch.exception;

// Ressource absente, ou invisible pour ce membre (blocage) : 404
public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(String message) {
        super(message);
    }
}
