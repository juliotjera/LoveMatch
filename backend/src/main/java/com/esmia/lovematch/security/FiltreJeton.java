package com.esmia.lovematch.security;

import com.esmia.lovematch.repository.UtilisateurRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// Authentifie chaque requête par son jeton ; un compte suspendu ou supprimé n'est plus reconnu, même avec un jeton valide
@Component
public class FiltreJeton extends OncePerRequestFilter {

    private final JetonService jetons;
    private final UtilisateurRepository utilisateurs;

    public FiltreJeton(JetonService jetons, UtilisateurRepository utilisateurs) {
        this.jetons = jetons;
        this.utilisateurs = utilisateurs;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requete, HttpServletResponse reponse, FilterChain chaine)
            throws ServletException, IOException {
        CookieSession.lire(requete)
            .flatMap(jetons::lire)
            .filter(membre -> utilisateurs.findById(membre.id()).map(u -> u.estActif()).orElse(false))
            .ifPresent(membre -> SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(membre, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + membre.role().name())))));
        chaine.doFilter(requete, reponse);
    }

    // l'interface compilée (fichiers statiques) n'a pas besoin d'authentification
    @Override
    protected boolean shouldNotFilter(HttpServletRequest requete) {
        String chemin = requete.getRequestURI();
        return !(chemin.startsWith("/api/") || chemin.startsWith("/ws"));
    }
}
