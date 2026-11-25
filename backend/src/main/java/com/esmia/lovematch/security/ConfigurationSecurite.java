package com.esmia.lovematch.security;

import com.esmia.lovematch.dto.response.ErreurResponse;
import com.esmia.lovematch.util.Horloge;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ConfigurationSecurite {

    @Bean
    public PasswordEncoder encodeurMotDePasse() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain chaineSecurite(HttpSecurity http, FiltreJeton filtreJeton, JsonMapper json, MessageSource messages)
            throws Exception {
        http
            // pas de session serveur : le jeton du cookie suffit
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // CSRF pour une application Angular : jeton dans le cookie XSRF-TOKEN, renvoyé dans l'en-tête X-XSRF-TOKEN
            // sans session côté serveur, chaque requête authentifiée ressemble à une « connexion » :
            // on garde le même jeton CSRF au lieu de le renouveler à chaque appel (requêtes parallèles de l'interface)
            .csrf(csrf -> csrf.spa().sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy()))
            .headers(h -> h
                .frameOptions(f -> f.deny())
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                // aucun script venu d'ailleurs ; les styles des composants Angular sont injectés dans la page
                .contentSecurityPolicy(c -> c.policyDirectives(
                    "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; "
                        + "font-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; "
                        + "form-action 'self'; frame-ancestors 'none'"))
                // LoveMatch ne demande jamais la position, la caméra ni le micro
                .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", "geolocation=(), camera=(), microphone=()")))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.POST, "/api/auth/inscription", "/api/auth/connexion", "/api/auth/demo/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/demo", "/api/referentiels/**").permitAll()
                .requestMatchers("/api/docs/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**", "/ws/**").authenticated()
                // l'interface Angular compilée (fichiers statiques et routes du navigateur)
                .anyRequest().permitAll())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, rep, ex) -> erreur(rep, json, HttpStatus.UNAUTHORIZED,
                    messages.getMessage("erreur.non.connecte", null, req.getLocale())))
                .accessDeniedHandler((req, rep, ex) -> erreur(rep, json, HttpStatus.FORBIDDEN,
                    messages.getMessage("erreur.acces", null, req.getLocale()))))
            .addFilterBefore(filtreJeton, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void erreur(HttpServletResponse reponse, JsonMapper json, HttpStatus statut, String message) throws IOException {
        reponse.setStatus(statut.value());
        reponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        reponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        json.writeValue(reponse.getWriter(), new ErreurResponse(statut.value(), statut.name(), message, null, Horloge.maintenant()));
    }
}
