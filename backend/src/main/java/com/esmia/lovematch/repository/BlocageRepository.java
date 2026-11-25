package com.esmia.lovematch.repository;

import com.esmia.lovematch.entity.Blocage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlocageRepository extends JpaRepository<Blocage, Long> {

    boolean existsByAuteurIdAndCibleId(Long auteurId, Long cibleId);

    @Query("select count(b) > 0 from Blocage b where (b.auteur.id = :a and b.cible.id = :b) or (b.auteur.id = :b and b.cible.id = :a)")
    boolean entre(@Param("a") Long a, @Param("b") Long b);

    @Query("select b from Blocage b join fetch b.cible where b.auteur.id = :auteur order by b.id desc")
    List<Blocage> deAuteur(@Param("auteur") Long auteur);

    void deleteByAuteurIdAndCibleId(Long auteurId, Long cibleId);
}
