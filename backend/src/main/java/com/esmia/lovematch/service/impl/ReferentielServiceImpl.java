package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.dto.response.InteretResponse;
import com.esmia.lovematch.dto.response.VilleResponse;
import com.esmia.lovematch.mapper.ProfilMapper;
import com.esmia.lovematch.repository.InteretRepository;
import com.esmia.lovematch.repository.VilleRepository;
import com.esmia.lovematch.service.ReferentielService;
import db.migration.V4__Villes_du_monde;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReferentielServiceImpl implements ReferentielService {

    private final VilleRepository villes;
    private final InteretRepository interets;
    private final ProfilMapper mapper;

    public ReferentielServiceImpl(VilleRepository villes, InteretRepository interets, ProfilMapper mapper) {
        this.villes = villes;
        this.interets = interets;
        this.mapper = mapper;
    }

    @Override
    public List<VilleResponse> villes(String debut, int limite) {
        String recherche = V4__Villes_du_monde.normaliser(debut == null ? "" : debut);
        if (recherche.isEmpty()) {
            return List.of();
        }
        return villes.rechercher(recherche + "%", PageRequest.of(0, limite)).stream().map(mapper::ville).toList();
    }

    @Override
    public List<InteretResponse> interets() {
        return interets.findAllByOrderByLibelleFrAsc().stream().map(mapper::interet).toList();
    }
}
