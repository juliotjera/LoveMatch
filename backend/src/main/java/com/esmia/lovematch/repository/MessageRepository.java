package com.esmia.lovematch.repository;

import com.esmia.lovematch.entity.Message;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // page de l'historique, du plus récent au plus ancien (l'interface les remet dans l'ordre)
    @Query("select m from Message m where m.correspondance.id = :c and (:avant is null or m.id < :avant) order by m.id desc")
    List<Message> historique(@Param("c") Long correspondance, @Param("avant") Long avant, Pageable page);

    Optional<Message> findFirstByCorrespondanceIdOrderByIdDesc(Long correspondanceId);

    long countByCorrespondanceIdAndAuteurIdNotAndLuLeIsNull(Long correspondanceId, Long lecteurId);

    @Modifying
    @Query("update Message m set m.luLe = :quand where m.correspondance.id = :c and m.auteur.id <> :lecteur and m.luLe is null")
    int marquerLus(@Param("c") Long correspondance, @Param("lecteur") Long lecteur, @Param("quand") LocalDateTime quand);

    long countByEnvoyeLeAfter(LocalDateTime depuis);
}
