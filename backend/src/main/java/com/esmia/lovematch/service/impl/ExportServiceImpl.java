package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.dto.response.ExportResponse;
import com.esmia.lovematch.dto.response.ExportResponse.BlocageExport;
import com.esmia.lovematch.dto.response.ExportResponse.MatchExport;
import com.esmia.lovematch.dto.response.ExportResponse.MessageExport;
import com.esmia.lovematch.dto.response.ExportResponse.PhotoExport;
import com.esmia.lovematch.dto.response.ExportResponse.ReactionExport;
import com.esmia.lovematch.dto.response.ExportResponse.SignalementExport;
import com.esmia.lovematch.entity.Correspondance;
import com.esmia.lovematch.entity.Message;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.exception.RessourceIntrouvableException;
import com.esmia.lovematch.mapper.ProfilMapper;
import com.esmia.lovematch.repository.BlocageRepository;
import com.esmia.lovematch.repository.CorrespondanceRepository;
import com.esmia.lovematch.repository.MessageRepository;
import com.esmia.lovematch.repository.PhotoRepository;
import com.esmia.lovematch.repository.ReactionRepository;
import com.esmia.lovematch.repository.SignalementRepository;
import com.esmia.lovematch.repository.UtilisateurRepository;
import com.esmia.lovematch.service.ExportService;
import com.esmia.lovematch.util.Horloge;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExportServiceImpl implements ExportService {

    private final UtilisateurRepository utilisateurs;
    private final PhotoRepository photos;
    private final ReactionRepository reactions;
    private final CorrespondanceRepository correspondances;
    private final MessageRepository messages;
    private final BlocageRepository blocages;
    private final SignalementRepository signalements;
    private final ProfilMapper mapper;

    public ExportServiceImpl(UtilisateurRepository utilisateurs, PhotoRepository photos, ReactionRepository reactions,
                             CorrespondanceRepository correspondances, MessageRepository messages,
                             BlocageRepository blocages, SignalementRepository signalements, ProfilMapper mapper) {
        this.utilisateurs = utilisateurs;
        this.photos = photos;
        this.reactions = reactions;
        this.correspondances = correspondances;
        this.messages = messages;
        this.blocages = blocages;
        this.signalements = signalements;
        this.mapper = mapper;
    }

    @Override
    public ExportResponse exporter(Long moi) {
        Utilisateur u = utilisateurs.findById(moi).orElseThrow(() -> new RessourceIntrouvableException("erreur.compte.introuvable"));

        List<PhotoExport> mesPhotos = photos.findByUtilisateurIdOrderByOrdreAsc(moi).stream()
            .map(p -> new PhotoExport(p.getOrdre(), p.getTypeMime(), Base64.getEncoder().encodeToString(p.getContenu())))
            .toList();

        List<ReactionExport> mesReactions = reactions.findByAuteurIdOrderByCreeLeDesc(moi).stream()
            .map(r -> new ReactionExport(r.getCible().getPrenom(), r.getDecision().name(), r.getCreeLe()))
            .toList();

        List<Correspondance> mesCorrespondances = correspondances.toutesDe(moi);
        Map<Long, List<Message>> parConversation = mesCorrespondances.isEmpty() ? Map.of()
            : messages.findByCorrespondanceIdInOrderByEnvoyeLeAsc(mesCorrespondances.stream().map(Correspondance::getId).toList())
                .stream().collect(Collectors.groupingBy(m -> m.getCorrespondance().getId()));
        List<MatchExport> mesMatchs = mesCorrespondances.stream()
            .map(c -> new MatchExport(c.autre(moi).getPrenom(), c.getCreeLe(), c.getFermeLe(),
                parConversation.getOrDefault(c.getId(), List.of()).stream()
                    .map(m -> new MessageExport(m.getAuteur().getId().equals(moi), m.getContenu(), m.getEnvoyeLe(), m.getLuLe()))
                    .toList()))
            .toList();

        List<BlocageExport> mesBlocages = blocages.deAuteur(moi).stream()
            .map(b -> new BlocageExport(b.getCible().getPrenom())).toList();

        List<SignalementExport> mesSignalements = signalements.findByAuteurIdOrderByCreeLeDesc(moi).stream()
            .map(s -> new SignalementExport(s.getCible().getPrenom(), s.getMotif().name(), s.getCommentaire(),
                s.getStatut().name(), s.getCreeLe()))
            .toList();

        return new ExportResponse(Horloge.maintenant(), mapper.moi(u), mesPhotos, mesReactions, mesMatchs, mesBlocages,
            mesSignalements);
    }
}
