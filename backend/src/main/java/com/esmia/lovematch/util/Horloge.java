package com.esmia.lovematch.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

// Heure de Madagascar pour toute l'application ; remplaçable dans les tests
public final class Horloge {

    public static final ZoneId MADAGASCAR = ZoneId.of("Indian/Antananarivo");

    private static Clock horloge = Clock.system(MADAGASCAR);

    private Horloge() {}

    public static LocalDateTime maintenant() {
        return LocalDateTime.now(horloge);
    }

    public static LocalDate aujourdhui() {
        return LocalDate.now(horloge);
    }

    public static void fixer(Clock autre) {
        horloge = autre;
    }

    public static void reinitialiser() {
        horloge = Clock.system(MADAGASCAR);
    }
}
