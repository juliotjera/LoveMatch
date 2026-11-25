package com.esmia.lovematch.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

// Envoie un événement dans la file personnelle d'un membre : /user/queue/evenements
@Component
public class TempsReel {

    public record Evenement(String type, Object donnees) {}

    private final SimpMessagingTemplate messagerie;

    public TempsReel(SimpMessagingTemplate messagerie) {
        this.messagerie = messagerie;
    }

    public void envoyer(Long membreId, String type, Object donnees) {
        messagerie.convertAndSendToUser(String.valueOf(membreId), "/queue/evenements", new Evenement(type, donnees));
    }
}
