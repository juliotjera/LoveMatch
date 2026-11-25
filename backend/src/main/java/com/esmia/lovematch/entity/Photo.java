package com.esmia.lovematch.entity;

import com.esmia.lovematch.util.Horloge;
import jakarta.persistence.Basic;
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

// Photo de profil, redimensionnée par le serveur ; gardée en base (l'hébergeur n'a pas de disque permanent)
@Entity
@Table(name = "photo")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] contenu;

    @Column(name = "type_mime", nullable = false, length = 30)
    private String typeMime;

    // 0 = photo principale
    @Column(nullable = false)
    private int ordre;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe = Horloge.maintenant();

    protected Photo() {}

    public Photo(Utilisateur utilisateur, byte[] contenu, String typeMime, int ordre) {
        this.utilisateur = utilisateur;
        this.contenu = contenu;
        this.typeMime = typeMime;
        this.ordre = ordre;
    }

    public Long getId() { return id; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public byte[] getContenu() { return contenu; }
    public String getTypeMime() { return typeMime; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
}
