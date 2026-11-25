package com.esmia.lovematch.repository;

import com.esmia.lovematch.entity.Ville;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VilleRepository extends JpaRepository<Ville, Long> {

    // recherche au fil de la frappe : début du nom, sans accents, les villes les plus peuplées d'abord
    @Query("select v from Ville v where v.nomAscii like :debut order by v.population desc, v.nom")
    List<Ville> rechercher(@Param("debut") String debut, Pageable page);

    Optional<Ville> findFirstByNomAsciiAndPaysOrderByPopulationDesc(String nomAscii, String pays);
}
