package com.esmia.lovematch.entity;

import com.esmia.lovematch.enumeration.Decision;
import com.esmia.lovematch.util.Horloge;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

// Like ou « passer » : une seule réaction par paire (auteur, cible), garantie par la base
@Entity
@Table(name = "reaction")
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auteur_id")
    private Utilisateur auteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cible_id")
    private Utilisateur cible;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Decision decision;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe = Horloge.maintenant();

    protected Reaction() {}

    public Reaction(Utilisateur auteur, Utilisateur cible, Decision decision) {
        this.auteur = auteur;
        this.cible = cible;
        this.decision = decision;
    }

    public Long getId() { return id; }
    public Utilisateur getAuteur() { return auteur; }
    public Utilisateur getCible() { return cible; }
    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }
    public LocalDateTime getCreeLe() { return creeLe; }
}
