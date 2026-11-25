package com.esmia.lovematch.dto.response;

import java.util.List;

// Score sur 100 et son détail : âge /30, distance /30, centres d'intérêt /30, même recherche /10
public record ScoreResponse(int total, int age, int distance, int interets, int langues, int recherche,
                            int distanceKm, List<String> interetsCommuns, List<String> languesCommunes) {}
