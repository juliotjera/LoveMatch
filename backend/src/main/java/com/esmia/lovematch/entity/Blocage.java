package com.esmia.lovematch.entity;

import com.esmia.lovematch.util.Horloge;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

// Blocage : la personne bloquée disparaît des suggestions, des matchs et de la messagerie, dans les deux sens
@Entity
@Table(name = "blocage")
public class Blocage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auteur_id")
    private Utilisateur auteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cible_id")
    private Utilisateur cible;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe = Horloge.maintenant();

    protected Blocage() {}

    public Blocage(Utilisateur auteur, Utilisateur cible) {
        this.auteur = auteur;
        this.cible = cible;
    }

    public Long getId() { return id; }
    public Utilisateur getAuteur() { return auteur; }
    public Utilisateur getCible() { return cible; }
}
