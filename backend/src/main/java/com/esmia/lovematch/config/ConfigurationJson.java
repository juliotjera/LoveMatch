package com.esmia.lovematch.config;

import com.esmia.lovematch.util.Horloge;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Les dates sont enregistrées à l'heure de Madagascar ; elles partent avec leur décalage (« 2026-11-25T10:15:00+03:00 »)
 * pour qu'un membre à Paris, Montréal ou Pékin les lise à sa propre heure. S'applique à l'API et au WebSocket.
 */
@Configuration
public class ConfigurationJson {

    @Bean
    public JacksonModule datesAvecFuseau() {
        return new SimpleModule("dates-avec-fuseau").addSerializer(LocalDateTime.class, new ValueSerializer<>() {
            @Override
            public void serialize(LocalDateTime date, JsonGenerator sortie, SerializationContext contexte) {
                sortie.writeString(date.atZone(Horloge.MADAGASCAR).toOffsetDateTime().toString());
            }
        });
    }
}
