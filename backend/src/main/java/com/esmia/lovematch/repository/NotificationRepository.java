package com.esmia.lovematch.repository;

import com.esmia.lovematch.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDestinataireIdOrderByIdDesc(Long destinataireId, Pageable page);

    long countByDestinataireIdAndLueFalse(Long destinataireId);

    @Modifying
    @Query("update Notification n set n.lue = true where n.destinataire.id = :membre and n.lue = false")
    int toutMarquerLu(@Param("membre") Long membre);
}
