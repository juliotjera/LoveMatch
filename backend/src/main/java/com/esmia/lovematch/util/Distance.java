package com.esmia.lovematch.util;

import com.esmia.lovematch.entity.Ville;

// Distance à vol d'oiseau entre deux villes (formule de haversine), en kilomètres
public final class Distance {

    private static final double RAYON_TERRE_KM = 6371.0;

    private Distance() {}

    public static double km(Ville a, Ville b) {
        if (a.getId() != null && a.getId().equals(b.getId())) {
            return 0;
        }
        double lat1 = Math.toRadians(a.getLatitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());
        double h = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * RAYON_TERRE_KM * Math.asin(Math.sqrt(h));
    }
}
