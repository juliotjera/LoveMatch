package com.esmia.lovematch.util;

import com.esmia.lovematch.entity.Interet;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.Recherche;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Score de compatibilité sur 100, expliqué critère par critère (sujet d'origine : âge, lieu, genre).
 *
 * <ul>
 *   <li>Filtres obligatoires : genre accepté dans les deux sens, âge de chacun dans la tranche recherchée par l'autre,
 *       distance dans le rayon de recherche des deux, au moins une langue en commun.</li>
 *   <li>Âge (25) : plein score au centre de la tranche recherchée, la moitié au bord ; moyenne des deux sens.</li>
 *   <li>Distance (25) : plein score sous 5 km, puis décroissance continue jusqu'au plus petit des deux rayons.</li>
 *   <li>Centres d'intérêt (25) : proportion d'intérêts communs (indice de Jaccard).</li>
 *   <li>Langues (15) : une langue commune 10, deux ou plus 15 (aucune : jamais proposés).</li>
 *   <li>Même recherche (10) : identique 10, l'un des deux « à découvrir » 5, sinon 0.</li>
 * </ul>
 *
 * Chaque critère est symétrique : le score de A pour B est égal au score de B pour A.
 */
public final class CalculScore {

    public static final int POINTS_AGE = 25;
    public static final int POINTS_DISTANCE = 25;
    public static final int POINTS_INTERETS = 25;
    public static final int POINTS_LANGUES = 15;
    public static final int POINTS_RECHERCHE = 10;
    private static final double DISTANCE_PLEIN_SCORE_KM = 5;

    private CalculScore() {}

    public record Detail(int total, int age, int distance, int interets, int langues, int recherche,
                         int distanceKm, List<String> interetsCommuns, List<String> languesCommunes) {}

    public static boolean compatibles(Utilisateur a, Utilisateur b) {
        return a.getGenreRecherche().accepte(b.getGenre())
            && b.getGenreRecherche().accepte(a.getGenre())
            && dansTranche(b.age(), a) && dansTranche(a.age(), b)
            && Distance.km(a.getVille(), b.getVille()) <= Math.min(a.getRayonKm(), b.getRayonKm())
            && !languesCommunes(a, b).isEmpty();
    }

    public static Detail calculer(Utilisateur a, Utilisateur b) {
        double km = Distance.km(a.getVille(), b.getVille());

        int age = (int) Math.round(POINTS_AGE * (proximiteAge(b.age(), a) + proximiteAge(a.age(), b)) / 2);

        double rayon = Math.min(a.getRayonKm(), b.getRayonKm());
        double partDistance = km <= DISTANCE_PLEIN_SCORE_KM ? 1
            : Math.max(0, 1 - (km - DISTANCE_PLEIN_SCORE_KM) / Math.max(1, rayon - DISTANCE_PLEIN_SCORE_KM));
        int distance = (int) Math.round(POINTS_DISTANCE * partDistance);

        Set<String> codesA = codes(a.getInterets());
        Set<String> codesB = codes(b.getInterets());
        Set<String> communs = new HashSet<>(codesA);
        communs.retainAll(codesB);
        Set<String> union = new HashSet<>(codesA);
        union.addAll(codesB);
        int interets = union.isEmpty() ? 0 : (int) Math.round(POINTS_INTERETS * communs.size() / (double) union.size());

        int recherche = a.getRecherche() == b.getRecherche() ? POINTS_RECHERCHE
            : (a.getRecherche() == Recherche.A_DECOUVRIR || b.getRecherche() == Recherche.A_DECOUVRIR) ? POINTS_RECHERCHE / 2 : 0;

        List<String> langues = languesCommunes(a, b);
        int pointsLangues = langues.isEmpty() ? 0 : langues.size() == 1 ? 10 : POINTS_LANGUES;

        List<String> communsTries = communs.stream().sorted().toList();
        return new Detail(age + distance + interets + pointsLangues + recherche, age, distance, interets, pointsLangues,
            recherche, (int) Math.round(km), communsTries, langues);
    }

    // codes des langues parlées par les deux, dans l'ordre de l'énumération
    public static List<String> languesCommunes(Utilisateur a, Utilisateur b) {
        return a.getLangues().stream().filter(b.getLangues()::contains).sorted().map(Enum::name).toList();
    }

    private static boolean dansTranche(int age, Utilisateur qui) {
        return age >= qui.getAgeMin() && age <= qui.getAgeMax();
    }

    // 1 au centre de la tranche recherchée par « qui », 0,5 au bord
    private static double proximiteAge(int age, Utilisateur qui) {
        double centre = (qui.getAgeMin() + qui.getAgeMax()) / 2.0;
        double demiLargeur = Math.max(1, (qui.getAgeMax() - qui.getAgeMin()) / 2.0);
        return Math.max(0.5, 1 - 0.5 * Math.abs(age - centre) / demiLargeur);
    }

    private static Set<String> codes(Set<Interet> interets) {
        Set<String> codes = new HashSet<>();
        for (Interet i : interets) {
            codes.add(i.getCode());
        }
        return codes;
    }
}
