package com.esmia.lovematch.repository;

import com.esmia.lovematch.entity.Signalement;
import com.esmia.lovematch.enumeration.StatutSignalement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SignalementRepository extends JpaRepository<Signalement, Long> {

    @Query("""
        select s from Signalement s join fetch s.auteur join fetch s.cible left join fetch s.message
        where s.statut = :statut order by s.creeLe asc
        """)
    List<Signalement> parStatut(@Param("statut") StatutSignalement statut);

    long countByStatut(StatutSignalement statut);

    long countByCibleId(Long cibleId);
}
