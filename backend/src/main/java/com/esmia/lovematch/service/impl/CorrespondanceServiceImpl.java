package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.config.TempsReel;
import com.esmia.lovematch.dto.response.CorrespondanceResponse;
import com.esmia.lovematch.entity.Correspondance;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.exception.RessourceIntrouvableException;
import com.esmia.lovematch.mapper.MessageMapper;
import com.esmia.lovematch.mapper.ProfilMapper;
import com.esmia.lovematch.repository.CorrespondanceRepository;
import com.esmia.lovematch.repository.MessageRepository;
import com.esmia.lovematch.service.CorrespondanceService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CorrespondanceServiceImpl implements CorrespondanceService {

    private final CorrespondanceRepository correspondances;
    private final MessageRepository messages;
    private final ProfilMapper profils;
    private final MessageMapper messageMapper;
    private final TempsReel tempsReel;

    public CorrespondanceServiceImpl(CorrespondanceRepository correspondances, MessageRepository messages,
                                     ProfilMapper profils, MessageMapper messageMapper, TempsReel tempsReel) {
        this.correspondances = correspondances;
        this.messages = messages;
        this.profils = profils;
        this.messageMapper = messageMapper;
        this.tempsReel = tempsReel;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorrespondanceResponse> mesMatchs(Long moi) {
        List<Correspondance> ouvertes = correspondances.ouvertesDe(moi);
        Map<Long, List<Long>> photos = profils.photosDe(ouvertes.stream().map(c -> c.autre(moi).getId()).toList());
        return ouvertes.stream()
            .map(c -> reponse(moi, c, photos.getOrDefault(c.autre(moi).getId(), List.of())))
            // conversations les plus actives d'abord : dernier message, sinon date du match
            .sorted(Comparator.comparing((CorrespondanceResponse r) ->
                r.dernierMessage() != null ? r.dernierMessage().envoyeLe() : r.creeLe()).reversed())
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CorrespondanceResponse unMatch(Long moi, Long correspondance) {
        Correspondance c = acces(moi, correspondance);
        return reponse(moi, c, profils.photosDe(c.autre(moi).getId()));
    }

    @Override
    public void fermer(Long moi, Long correspondance) {
        Correspondance c = acces(moi, correspondance);
        c.fermer();
        Long autre = c.autre(moi).getId();
        NotificationServiceImpl.apresValidation(() -> tempsReel.envoyer(autre, "MATCH_FERME", c.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Correspondance acces(Long moi, Long correspondance) {
        return correspondances.findById(correspondance).filter(c -> c.concerne(moi))
            .orElseThrow(() -> new RessourceIntrouvableException("erreur.conversation.introuvable"));
    }

    private CorrespondanceResponse reponse(Long moi, Correspondance c, List<Long> photos) {
        Utilisateur autre = c.autre(moi);
        var dernier = messages.findFirstByCorrespondanceIdOrderByIdDesc(c.getId()).map(messageMapper::reponse).orElse(null);
        long nonLus = messages.countByCorrespondanceIdAndAuteurIdNotAndLuLeIsNull(c.getId(), moi);
        return new CorrespondanceResponse(c.getId(), profils.publicDe(autre, photos), c.getCreeLe(), c.estOuverte(), dernier, nonLus);
    }
}
