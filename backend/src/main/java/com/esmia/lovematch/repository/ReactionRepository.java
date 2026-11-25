package com.esmia.lovematch.repository;

import com.esmia.lovematch.entity.Reaction;
import com.esmia.lovematch.enumeration.Decision;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByAuteurIdAndCibleId(Long auteurId, Long cibleId);

    boolean existsByAuteurIdAndCibleIdAndDecision(Long auteurId, Long cibleId, Decision decision);

    long countByAuteurIdAndDecisionAndCreeLeAfter(Long auteurId, Decision decision, LocalDateTime depuis);

    long countByCibleIdAndDecision(Long cibleId, Decision decision);

    List<Reaction> findByAuteurIdOrderByCreeLeDesc(Long auteurId);

    // likes reçus en attente : le membre n'a pas encore répondu, l'auteur est actif et rien ne les bloque
    @Query("""
        select count(r) from Reaction r
        where r.cible.id = :moi
          and r.decision = com.esmia.lovematch.enumeration.Decision.LIKE
          and r.auteur.statut = com.esmia.lovematch.enumeration.StatutCompte.ACTIF
          and not exists (select m from Reaction m where m.auteur.id = :moi and m.cible = r.auteur)
          and not exists (select b from Blocage b
                          where (b.auteur.id = :moi and b.cible = r.auteur) or (b.auteur = r.auteur and b.cible.id = :moi))
        """)
    long likesEnAttente(@Param("moi") Long moi);
}
