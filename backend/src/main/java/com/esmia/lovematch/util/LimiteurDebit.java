package com.esmia.lovematch.util;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

// Limitation de débit en mémoire, par clé (« like:12 », « connexion:41.188.12.0 »…), sur une fenêtre glissante
@Component
public class LimiteurDebit {

    private final Map<String, Deque<Long>> historiques = new ConcurrentHashMap<>();

    // true si l'action est permise (et la compte), false si la limite est atteinte
    public boolean autoriser(String cle, int maximum, Duration fenetre) {
        long maintenant = System.nanoTime();
        long debut = maintenant - fenetre.toNanos();
        Deque<Long> instants = historiques.computeIfAbsent(cle, k -> new ArrayDeque<>());
        synchronized (instants) {
            while (!instants.isEmpty() && instants.peekFirst() < debut) {
                instants.pollFirst();
            }
            if (instants.size() >= maximum) {
                return false;
            }
            instants.addLast(maintenant);
            return true;
        }
    }

    public void reinitialiser() {
        historiques.clear();
    }
}
