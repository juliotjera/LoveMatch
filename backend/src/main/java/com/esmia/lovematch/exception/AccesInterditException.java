package com.esmia.lovematch.exception;

// Ressource existante mais réservée à d'autres : 403
public class AccesInterditException extends RuntimeException {

    public AccesInterditException(String message) {
        super(message);
    }
}
