package com.esmia.lovematch.config;

import com.esmia.lovematch.security.CookieSession;
import com.esmia.lovematch.security.JetonService;
import com.esmia.lovematch.security.MembreConnecte;
import com.esmia.lovematch.repository.UtilisateurRepository;
import java.security.Principal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * Temps réel (STOMP sur WebSocket) : le serveur pousse les notifications et les messages dans la file
 * personnelle de chaque membre (/user/queue/evenements). Les envois passent par l'API REST (protégée par CSRF) :
 * le navigateur ne fait qu'écouter. La poignée de main est authentifiée par le même cookie que l'API.
 */
@Configuration
@EnableWebSocketMessageBroker
public class ConfigurationWebSocket implements WebSocketMessageBrokerConfigurer {

    private static final String MEMBRE = "membre";

    private final JetonService jetons;
    private final UtilisateurRepository utilisateurs;
    private final String[] origines;

    public ConfigurationWebSocket(JetonService jetons, UtilisateurRepository utilisateurs,
                                  @Value("${lovematch.origines}") String[] origines) {
        this.jetons = jetons;
        this.utilisateurs = utilisateurs;
        this.origines = origines;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registre) {
        registre.enableSimpleBroker("/queue");
        registre.setUserDestinationPrefix("/user");
        registre.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registre) {
        registre.addEndpoint("/ws")
            .setAllowedOriginPatterns(origines)
            .addInterceptors(new HandshakeInterceptor() {
                @Override
                public boolean beforeHandshake(ServerHttpRequest requete, ServerHttpResponse reponse,
                                               WebSocketHandler gestionnaire, Map<String, Object> attributs) {
                    if (!(requete instanceof ServletServerHttpRequest servlet)) {
                        return false;
                    }
                    return CookieSession.lire(servlet.getServletRequest())
                        .flatMap(jetons::lire)
                        .filter(m -> utilisateurs.findById(m.id()).map(u -> u.estActif()).orElse(false))
                        .map(m -> { attributs.put(MEMBRE, m); return true; })
                        .orElse(false);
                }

                @Override
                public void afterHandshake(ServerHttpRequest requete, ServerHttpResponse reponse,
                                           WebSocketHandler gestionnaire, Exception erreur) {}
            })
            .setHandshakeHandler(new DefaultHandshakeHandler() {
                @Override
                protected Principal determineUser(ServerHttpRequest requete, WebSocketHandler gestionnaire,
                                                  Map<String, Object> attributs) {
                    return (MembreConnecte) attributs.get(MEMBRE);
                }
            });
    }
}
