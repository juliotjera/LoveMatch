package com.esmia.lovematch.entity;

import com.esmia.lovematch.enumeration.TypeNotification;
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

// Like (anonyme), match ou message. Le texte est composé par l'interface, dans la langue choisie.
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinataire_id")
    private Utilisateur destinataire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TypeNotification type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correspondance_id")
    private Correspondance correspondance;

    @Column(nullable = false)
    private boolean lue;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe = Horloge.maintenant();

    protected Notification() {}

    public Notification(Utilisateur destinataire, TypeNotification type, Correspondance correspondance) {
        this.destinataire = destinataire;
        this.type = type;
        this.correspondance = correspondance;
    }

    public Long getId() { return id; }
    public Utilisateur getDestinataire() { return destinataire; }
    public TypeNotification getType() { return type; }
    public Correspondance getCorrespondance() { return correspondance; }
    public boolean isLue() { return lue; }
    public void setLue(boolean lue) { this.lue = lue; }
    public LocalDateTime getCreeLe() { return creeLe; }
}
