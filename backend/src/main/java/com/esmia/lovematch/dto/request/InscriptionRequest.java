package com.esmia.lovematch.dto.request;

import com.esmia.lovematch.enumeration.Genre;
import com.esmia.lovematch.enumeration.GenreRecherche;
import com.esmia.lovematch.enumeration.Langue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record InscriptionRequest(
    @NotBlank @Email @Size(max = 190) String email,
    @NotBlank @Size(min = 8, max = 72, message = "{motDePasse.taille}") String motDePasse,
    @NotBlank @Size(max = 60) String prenom,
    @NotBlank @Size(max = 60) String nom,
    @NotNull @Past LocalDate dateNaissance,
    @NotNull Genre genre,
    @NotNull Long villeId,
    @NotNull GenreRecherche genreRecherche,
    // facultatif : par défaut la langue choisie dans l'interface
    @Size(max = 5, message = "{langues.nombre}") List<Langue> langues
) {}
