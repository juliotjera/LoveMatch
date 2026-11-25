package com.esmia.lovematch;

import static org.assertj.core.api.Assertions.assertThat;

import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.Role;
import com.esmia.lovematch.repository.UtilisateurRepository;
import com.esmia.lovematch.service.DemoService;
import com.esmia.lovematch.util.CalculScore;
import com.esmia.lovematch.util.CalculScore.Detail;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// Critère 8.4 : le score ne dépend pas du sens du calcul ; vérifié sur toutes les paires du jeu de démonstration
@SpringBootTest(properties = "lovematch.demo=false")
@Transactional
class ScoreTest {

    @Autowired DemoService demo;
    @Autowired UtilisateurRepository utilisateurs;

    @Test
    @DisplayName("8.4 — score(A, B) = score(B, A), toujours entre 0 et 100")
    void scoreSymetrique() {
        demo.preparer();
        List<Utilisateur> membres = utilisateurs.findAll().stream().filter(u -> u.getRole() == Role.MEMBRE).toList();
        assertThat(membres).hasSizeGreaterThan(20);

        for (Utilisateur a : membres) {
            for (Utilisateur b : membres) {
                if (a == b) {
                    continue;
                }
                Detail ab = CalculScore.calculer(a, b);
                Detail ba = CalculScore.calculer(b, a);
                assertThat(ab).as(a.getPrenom() + " / " + b.getPrenom()).isEqualTo(ba);
                assertThat(ab.total()).isBetween(0, 100)
                    .isEqualTo(ab.age() + ab.distance() + ab.interets() + ab.langues() + ab.recherche());
                assertThat(CalculScore.compatibles(a, b)).isEqualTo(CalculScore.compatibles(b, a));
            }
        }
    }

    @Test
    @DisplayName("Plus on partage, plus le score est haut")
    void scoreCoherent() {
        demo.preparer();
        Utilisateur fanja = parPrenom("Fanja");
        // Tiana : même ville, 4 intérêts communs ; Njaka : 127 km, aucun intérêt commun
        assertThat(CalculScore.calculer(fanja, parPrenom("Tiana")).total())
            .isGreaterThan(CalculScore.calculer(fanja, parPrenom("Njaka")).total());
        assertThat(CalculScore.calculer(fanja, parPrenom("Tiana")).distanceKm()).isZero();
        // Mahery est à Toamasina, au-delà de son rayon de 100 km : jamais proposé
        assertThat(CalculScore.compatibles(fanja, parPrenom("Mahery"))).isFalse();
        // Jun ne parle que chinois, Fanja malgache, français et anglais : aucune langue commune, jamais proposés
        assertThat(CalculScore.compatibles(fanja, parPrenom("Jun"))).isFalse();
        // Diego parle espagnol et anglais : l'anglais suffit, une langue commune vaut 10 points
        CalculScore.Detail diego = CalculScore.calculer(fanja, parPrenom("Diego"));
        assertThat(CalculScore.compatibles(fanja, parPrenom("Diego"))).isTrue();
        assertThat(diego.languesCommunes()).containsExactly("EN");
        assertThat(diego.langues()).isEqualTo(10);
        // Fanja et Hery partagent malgache, français et anglais : plein score
        assertThat(CalculScore.calculer(fanja, parPrenom("Hery")).langues()).isEqualTo(CalculScore.POINTS_LANGUES);
    }

    private Utilisateur parPrenom(String prenom) {
        return utilisateurs.findAll().stream().filter(u -> u.isDemo() && u.getPrenom().equals(prenom)).findFirst().orElseThrow();
    }
}
