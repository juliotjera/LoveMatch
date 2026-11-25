package com.esmia.lovematch.repository;

import com.esmia.lovematch.entity.Ville;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VilleRepository extends JpaRepository<Ville, Long> {

    List<Ville> findAllByOrderByNomAsc();
}
