package com.esmia.lovematch.security;

import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.Role;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

// Jeton de session signé par l'application (HMAC SHA-256) : identifiant du membre, rôle, expiration
@Service
public class JetonService {

    private final JwtEncoder encodeur;
    private final JwtDecoder decodeur;
    private final Duration duree;

    public JetonService(@Value("${lovematch.jeton.secret}") String secret,
                        @Value("${lovematch.jeton.duree-heures}") long dureeHeures) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("lovematch.jeton.secret doit contenir au moins 32 caractères");
        }
        SecretKey cle = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.encodeur = NimbusJwtEncoder.withSecretKey(cle).build();
        this.decodeur = NimbusJwtDecoder.withSecretKey(cle).macAlgorithm(MacAlgorithm.HS256).build();
        this.duree = Duration.ofHours(dureeHeures);
    }

    public String creer(Utilisateur utilisateur) {
        Instant maintenant = Instant.now();
        JwtClaimsSet revendications = JwtClaimsSet.builder()
            .issuer("lovematch")
            .subject(String.valueOf(utilisateur.getId()))
            .claim("role", utilisateur.getRole().name())
            .issuedAt(maintenant)
            .expiresAt(maintenant.plus(duree))
            .build();
        return encodeur.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), revendications)).getTokenValue();
    }

    public Optional<MembreConnecte> lire(String jeton) {
        try {
            Jwt jwt = decodeur.decode(jeton);
            return Optional.of(new MembreConnecte(Long.valueOf(jwt.getSubject()), Role.valueOf(jwt.getClaimAsString("role"))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Duration duree() {
        return duree;
    }
}
