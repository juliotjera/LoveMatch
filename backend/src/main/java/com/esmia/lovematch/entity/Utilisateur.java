package com.esmia.lovematch.entity;

import com.esmia.lovematch.enumeration.Genre;
import com.esmia.lovematch.enumeration.GenreRecherche;
import com.esmia.lovematch.enumeration.Langue;
import com.esmia.lovematch.enumeration.Recherche;
import com.esmia.lovematch.enumeration.Role;
import com.esmia.lovematch.enumeration.StatutCompte;
import com.esmia.lovematch.util.Horloge;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.LinkedHashSet;
import java.util.Set;
import org.hibernate.annotations.BatchSize;

// Membre (ou administrateur). Le nom de famille et l'e-mail ne sont jamais renvoyés aux autres membres.
@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 100)
    private String motDePasse;

    @Column(nullable = false, length = 60)
    private String prenom;

    @Column(nullable = false, length = 60)
    private String nom;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Genre genre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ville_id")
    private Ville ville;

    @Column(length = 500)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Recherche recherche = Recherche.A_DECOUVRIR;

    @Enumerated(EnumType.STRING)
    @Column(name = "genre_recherche", nullable = false, length = 10)
    private GenreRecherche genreRecherche;

    @Column(name = "age_min", nullable = false)
    private int ageMin = 18;

    @Column(name = "age_max", nullable = false)
    private int ageMax = 35;

    @Column(name = "rayon_km", nullable = false)
    private int rayonKm = 100;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.MEMBRE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatutCompte statut = StatutCompte.ACTIF;

    @Column(nullable = false)
    private boolean demo;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe = Horloge.maintenant();

    @Column(name = "derniere_activite", nullable = false)
    private LocalDateTime derniereActivite = Horloge.maintenant();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "utilisateur_interet",
        joinColumns = @JoinColumn(name = "utilisateur_id"),
        inverseJoinColumns = @JoinColumn(name = "interet_id"))
    @BatchSize(size = 64)
    private Set<Interet> interets = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "utilisateur_langue", joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "langue", length = 2, nullable = false)
    @Enumerated(EnumType.STRING)
    @BatchSize(size = 64)
    private Set<Langue> langues = new LinkedHashSet<>();

    public int age() {
        return Period.between(dateNaissance, Horloge.aujourdhui()).getYears();
    }

    public boolean estActif() {
        return statut == StatutCompte.ACTIF;
    }

    public boolean estAdmin() {
        return role == Role.ADMIN;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }
    public Ville getVille() { return ville; }
    public void setVille(Ville ville) { this.ville = ville; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public Recherche getRecherche() { return recherche; }
    public void setRecherche(Recherche recherche) { this.recherche = recherche; }
    public GenreRecherche getGenreRecherche() { return genreRecherche; }
    public void setGenreRecherche(GenreRecherche genreRecherche) { this.genreRecherche = genreRecherche; }
    public int getAgeMin() { return ageMin; }
    public void setAgeMin(int ageMin) { this.ageMin = ageMin; }
    public int getAgeMax() { return ageMax; }
    public void setAgeMax(int ageMax) { this.ageMax = ageMax; }
    public int getRayonKm() { return rayonKm; }
    public void setRayonKm(int rayonKm) { this.rayonKm = rayonKm; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public StatutCompte getStatut() { return statut; }
    public void setStatut(StatutCompte statut) { this.statut = statut; }
    public boolean isDemo() { return demo; }
    public void setDemo(boolean demo) { this.demo = demo; }
    public LocalDateTime getCreeLe() { return creeLe; }
    public LocalDateTime getDerniereActivite() { return derniereActivite; }
    public void setDerniereActivite(LocalDateTime derniereActivite) { this.derniereActivite = derniereActivite; }
    public Set<Interet> getInterets() { return interets; }
    public Set<Langue> getLangues() { return langues; }
}
