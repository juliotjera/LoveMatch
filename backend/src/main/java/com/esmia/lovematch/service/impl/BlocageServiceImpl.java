package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.dto.response.ProfilPublicResponse;
import com.esmia.lovematch.entity.Blocage;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.exception.RegleMetierException;
import com.esmia.lovematch.exception.RessourceIntrouvableException;
import com.esmia.lovematch.mapper.ProfilMapper;
import com.esmia.lovematch.repository.BlocageRepository;
import com.esmia.lovematch.repository.CorrespondanceRepository;
import com.esmia.lovematch.repository.UtilisateurRepository;
import com.esmia.lovematch.service.BlocageService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Bloquer ferme aussi le match éventuel ; la personne bloquée n'est pas prévenue
@Service
@Transactional
public class BlocageServiceImpl implements BlocageService {

    private final BlocageRepository blocages;
    private final UtilisateurRepository utilisateurs;
    private final CorrespondanceRepository correspondances;
    private final ProfilMapper mapper;

    public BlocageServiceImpl(BlocageRepository blocages, UtilisateurRepository utilisateurs,
                              CorrespondanceRepository correspondances, ProfilMapper mapper) {
        this.blocages = blocages;
        this.utilisateurs = utilisateurs;
        this.correspondances = correspondances;
        this.mapper = mapper;
    }

    @Override
    public void bloquer(Long moi, Long cible) {
        if (moi.equals(cible)) {
            throw new RegleMetierException("erreur.bloquer.soi");
        }
        Utilisateur bloque = utilisateurs.findById(cible).orElseThrow(() -> new RessourceIntrouvableException("erreur.profil.introuvable"));
        if (!blocages.existsByAuteurIdAndCibleId(moi, cible)) {
            blocages.save(new Blocage(utilisateurs.getReferenceById(moi), bloque));
        }
        correspondances.paire(Math.min(moi, cible), Math.max(moi, cible)).ifPresent(c -> c.fermer());
    }

    @Override
    public void debloquer(Long moi, Long cible) {
        blocages.deleteByAuteurIdAndCibleId(moi, cible);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfilPublicResponse> bloques(Long moi) {
        return blocages.deAuteur(moi).stream().map(b -> mapper.publicDe(b.getCible(), List.of())).toList();
    }
}
