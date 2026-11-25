package com.esmia.lovematch.entity;

import com.esmia.lovematch.enumeration.MotifSignalement;
import com.esmia.lovematch.enumeration.StatutSignalement;
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

@Entity
@Table(name = "signalement")
public class Signalement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auteur_id")
    private Utilisateur auteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cible_id")
    private Utilisateur cible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MotifSignalement motif;

    @Column(length = 500)
    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatutSignalement statut = StatutSignalement.OUVERT;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe = Horloge.maintenant();

    @Column(name = "traite_le")
    private LocalDateTime traiteLe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traite_par")
    private Utilisateur traitePar;

    protected Signalement() {}

    public Signalement(Utilisateur auteur, Utilisateur cible, Message message, MotifSignalement motif, String commentaire) {
        this.auteur = auteur;
        this.cible = cible;
        this.message = message;
        this.motif = motif;
        this.commentaire = commentaire;
    }

    public void traiter(StatutSignalement decision, Utilisateur administrateur) {
        this.statut = decision;
        this.traiteLe = Horloge.maintenant();
        this.traitePar = administrateur;
    }

    public Long getId() { return id; }
    public Utilisateur getAuteur() { return auteur; }
    public Utilisateur getCible() { return cible; }
    public Message getMessage() { return message; }
    public MotifSignalement getMotif() { return motif; }
    public String getCommentaire() { return commentaire; }
    public StatutSignalement getStatut() { return statut; }
    public LocalDateTime getCreeLe() { return creeLe; }
    public LocalDateTime getTraiteLe() { return traiteLe; }
}
