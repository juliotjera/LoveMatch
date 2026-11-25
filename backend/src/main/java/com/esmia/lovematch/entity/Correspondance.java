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

// Match mutuel entre deux membres (membreA = plus petit identifiant : une seule ligne par paire).
// Une correspondance fermée (blocage, « ne plus être en contact ») ne permet plus d'écrire.
@Entity
@Table(name = "correspondance")
public class Correspondance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_a_id")
    private Utilisateur membreA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_b_id")
    private Utilisateur membreB;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe = Horloge.maintenant();

    @Column(name = "ferme_le")
    private LocalDateTime fermeLe;

    protected Correspondance() {}

    public Correspondance(Utilisateur un, Utilisateur autre) {
        boolean ordre = un.getId() < autre.getId();
        this.membreA = ordre ? un : autre;
        this.membreB = ordre ? autre : un;
    }

    public boolean concerne(Long utilisateurId) {
        return membreA.getId().equals(utilisateurId) || membreB.getId().equals(utilisateurId);
    }

    public Utilisateur autre(Long utilisateurId) {
        return membreA.getId().equals(utilisateurId) ? membreB : membreA;
    }

    public boolean estOuverte() {
        return fermeLe == null;
    }

    public void fermer() {
        if (fermeLe == null) {
            fermeLe = Horloge.maintenant();
        }
    }

    public Long getId() { return id; }
    public Utilisateur getMembreA() { return membreA; }
    public Utilisateur getMembreB() { return membreB; }
    public LocalDateTime getCreeLe() { return creeLe; }
    public LocalDateTime getFermeLe() { return fermeLe; }
}
