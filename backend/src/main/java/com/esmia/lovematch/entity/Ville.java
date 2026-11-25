package com.esmia.lovematch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Ville (Madagascar et villes du monde de plus de 15 000 habitants, GeoNames) : la distance entre deux membres
// se calcule entre leurs villes, jamais à partir d'une position précise
@Entity
@Table(name = "ville")
public class Ville {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(nullable = false, length = 120)
    private String region;

    // code ISO 3166 du pays (« MG », « FR »…), affiché dans la langue de l'interface
    @Column(nullable = false, length = 2)
    private String pays;

    // nom en minuscules sans accents, pour la recherche
    @Column(name = "nom_ascii", nullable = false, length = 120)
    private String nomAscii;

    @Column(nullable = false)
    private int population;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getRegion() { return region; }
    public String getPays() { return pays; }
    public int getPopulation() { return population; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
