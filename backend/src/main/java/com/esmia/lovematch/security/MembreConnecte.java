package com.esmia.lovematch.security;

import com.esmia.lovematch.enumeration.Role;
import java.security.Principal;

// Personne authentifiée par son jeton : identifiant et rôle (le nom du Principal sert aux files WebSocket)
public record MembreConnecte(Long id, Role role) implements Principal {

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}
