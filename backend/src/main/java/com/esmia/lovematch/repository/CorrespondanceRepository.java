package com.esmia.lovematch.repository;

import com.esmia.lovematch.entity.Correspondance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CorrespondanceRepository extends JpaRepository<Correspondance, Long> {

    @Query("select c from Correspondance c where c.membreA.id = :a and c.membreB.id = :b")
    Optional<Correspondance> paire(@Param("a") Long a, @Param("b") Long b);

    // matchs encore ouverts d'un membre, les plus récents d'abord
    @Query("""
        select c from Correspondance c
        join fetch c.membreA join fetch c.membreB
        where (c.membreA.id = :membre or c.membreB.id = :membre) and c.fermeLe is null
        order by c.creeLe desc
        """)
    List<Correspondance> ouvertesDe(@Param("membre") Long membre);

    long countByFermeLeIsNull();

    // toutes les correspondances du membre, même fermées (export de ses données)
    @Query("select c from Correspondance c where c.membreA.id = :membre or c.membreB.id = :membre order by c.creeLe desc")
    List<Correspondance> toutesDe(@Param("membre") Long membre);
}
