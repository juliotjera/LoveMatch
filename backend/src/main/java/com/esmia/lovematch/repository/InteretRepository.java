package com.esmia.lovematch.repository;

import com.esmia.lovematch.entity.Interet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteretRepository extends JpaRepository<Interet, Long> {

    List<Interet> findAllByOrderByLibelleFrAsc();

    List<Interet> findByCodeIn(List<String> codes);
}
