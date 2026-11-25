package com.esmia.lovematch.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

// Le jeton voyage dans un cookie HttpOnly (inaccessible au JavaScript, donc à une faille XSS) et SameSite=Strict
@Component
public class CookieSession {

    public static final String NOM = "LM_SESSION";

    private final boolean securise;

    public CookieSession(@Value("${lovematch.cookie.securise}") boolean securise) {
        this.securise = securise;
    }

    public ResponseCookie creer(String jeton, Duration duree) {
        return ResponseCookie.from(NOM, jeton).httpOnly(true).secure(securise).sameSite("Strict").path("/").maxAge(duree).build();
    }

    public ResponseCookie effacer() {
        return ResponseCookie.from(NOM, "").httpOnly(true).secure(securise).sameSite("Strict").path("/").maxAge(0).build();
    }

    public static Optional<String> lire(HttpServletRequest requete) {
        Cookie[] cookies = requete.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (NOM.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                    return Optional.of(cookie.getValue());
                }
            }
        }
        // clients de l'API autres que le navigateur (Postman, tests) : en-tête Authorization: Bearer
        String entete = requete.getHeader("Authorization");
        if (entete != null && entete.startsWith("Bearer ")) {
            return Optional.of(entete.substring(7));
        }
        return Optional.empty();
    }
}
