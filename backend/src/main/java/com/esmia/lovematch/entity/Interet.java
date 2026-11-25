package com.esmia.lovematch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Centre d'intérêt, libellé en français et en malagasy
@Entity
@Table(name = "interet")
public class Interet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "libelle_fr", nullable = false, length = 60)
    private String libelleFr;

    @Column(name = "libelle_mg", nullable = false, length = 60)
    private String libelleMg;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getLibelleFr() { return libelleFr; }
    public String getLibelleMg() { return libelleMg; }
}
