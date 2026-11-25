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

@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "correspondance_id")
    private Correspondance correspondance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auteur_id")
    private Utilisateur auteur;

    @Column(nullable = false, length = 1000)
    private String contenu;

    @Column(name = "envoye_le", nullable = false)
    private LocalDateTime envoyeLe = Horloge.maintenant();

    @Column(name = "lu_le")
    private LocalDateTime luLe;

    protected Message() {}

    public Message(Correspondance correspondance, Utilisateur auteur, String contenu) {
        this.correspondance = correspondance;
        this.auteur = auteur;
        this.contenu = contenu;
    }

    public Long getId() { return id; }
    public Correspondance getCorrespondance() { return correspondance; }
    public Utilisateur getAuteur() { return auteur; }
    public String getContenu() { return contenu; }
    public LocalDateTime getEnvoyeLe() { return envoyeLe; }
    public LocalDateTime getLuLe() { return luLe; }
    public void setLuLe(LocalDateTime luLe) { this.luLe = luLe; }
    // données de démonstration : une conversation étalée dans le temps
    public void antidater(LocalDateTime envoyeLe) { this.envoyeLe = envoyeLe; }
}
