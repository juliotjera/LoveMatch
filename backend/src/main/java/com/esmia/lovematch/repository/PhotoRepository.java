package com.esmia.lovematch.repository;

import com.esmia.lovematch.entity.Photo;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findByUtilisateurIdOrderByOrdreAsc(Long utilisateurId);

    long countByUtilisateurId(Long utilisateurId);

    // identifiants des photos de plusieurs membres, sans charger les images (liste de suggestions)
    @Query("select p.utilisateur.id, p.id from Photo p where p.utilisateur.id in :membres order by p.ordre")
    List<Object[]> identifiantsParMembre(@Param("membres") Collection<Long> membres);
}
