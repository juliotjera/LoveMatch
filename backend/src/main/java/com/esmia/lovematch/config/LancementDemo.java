package com.esmia.lovematch.config;

import com.esmia.lovematch.service.DemoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// La démonstration est recréée au démarrage puis chaque nuit : les visiteurs partagent les mêmes comptes fictifs
@Component
@ConditionalOnProperty(name = "lovematch.demo", havingValue = "true")
public class LancementDemo {

    private final DemoService demo;

    public LancementDemo(DemoService demo) {
        this.demo = demo;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void auDemarrage() {
        demo.preparer();
    }

    @Scheduled(cron = "0 0 4 * * *", zone = "Indian/Antananarivo")
    public void chaqueNuit() {
        demo.preparer();
    }
}
