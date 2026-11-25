package com.esmia.lovematch.dto.response;

import com.esmia.lovematch.enumeration.Genre;
import com.esmia.lovematch.enumeration.GenreRecherche;
import com.esmia.lovematch.enumeration.Langue;
import com.esmia.lovematch.enumeration.Recherche;
import com.esmia.lovematch.enumeration.Role;
import java.time.LocalDate;
import java.util.List;

// Profil complet, renvoyé uniquement à son propriétaire
public record MoiResponse(
    Long id, String email, String prenom, String nom, LocalDate dateNaissance, int age, Genre genre,
    VilleResponse ville, String bio, Recherche recherche, List<InteretResponse> interets, List<Langue> langues, List<Long> photos,
    GenreRecherche genreRecherche, int ageMin, int ageMax, int rayonKm, Role role, boolean demo, boolean profilComplet
) {}
