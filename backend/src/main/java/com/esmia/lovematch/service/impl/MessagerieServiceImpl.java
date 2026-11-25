package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.config.TempsReel;
import com.esmia.lovematch.dto.response.MessageResponse;
import com.esmia.lovematch.entity.Correspondance;
import com.esmia.lovematch.entity.Message;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.TypeNotification;
import com.esmia.lovematch.exception.RegleMetierException;
import com.esmia.lovematch.exception.TropDeDemandesException;
import com.esmia.lovematch.mapper.MessageMapper;
import com.esmia.lovematch.repository.BlocageRepository;
import com.esmia.lovematch.repository.MessageRepository;
import com.esmia.lovematch.repository.NotificationRepository;
import com.esmia.lovematch.repository.UtilisateurRepository;
import com.esmia.lovematch.service.CorrespondanceService;
import com.esmia.lovematch.service.MessagerieService;
import com.esmia.lovematch.service.NotificationService;
import com.esmia.lovematch.util.Horloge;
import com.esmia.lovematch.util.LimiteurDebit;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Messagerie : seulement entre deux personnes qui ont matché, dans une conversation encore ouverte
@Service
@Transactional
public class MessagerieServiceImpl implements MessagerieService {

    public static final int MESSAGES_PAR_MINUTE = 30;

    private final CorrespondanceService correspondances;
    private final MessageRepository messages;
    private final UtilisateurRepository utilisateurs;
    private final BlocageRepository blocages;
    private final NotificationRepository notificationsEnBase;
    private final NotificationService notifications;
    private final MessageMapper mapper;
    private final TempsReel tempsReel;
    private final LimiteurDebit limiteur;

    public MessagerieServiceImpl(CorrespondanceService correspondances, MessageRepository messages,
                                 UtilisateurRepository utilisateurs, BlocageRepository blocages,
                                 NotificationRepository notificationsEnBase, NotificationService notifications,
                                 MessageMapper mapper, TempsReel tempsReel, LimiteurDebit limiteur) {
        this.correspondances = correspondances;
        this.messages = messages;
        this.utilisateurs = utilisateurs;
        this.blocages = blocages;
        this.notificationsEnBase = notificationsEnBase;
        this.notifications = notifications;
        this.mapper = mapper;
        this.tempsReel = tempsReel;
        this.limiteur = limiteur;
    }

    @Override
    public List<MessageResponse> historique(Long moi, Long correspondance, Long avant, int taille) {
        Correspondance c = correspondances.acces(moi, correspondance);
        List<MessageResponse> page = new ArrayList<>(messages.historique(c.getId(), avant, PageRequest.of(0, taille))
            .stream().map(mapper::reponse).toList());
        Collections.reverse(page);
        // ouvrir la conversation marque comme lus les messages reçus, et prévient l'autre (« Vu »)
        if (avant == null && messages.marquerLus(c.getId(), moi, Horloge.maintenant()) > 0) {
            Long autre = c.autre(moi).getId();
            NotificationServiceImpl.apresValidation(() -> tempsReel.envoyer(autre, "LU", c.getId()));
        }
        return page;
    }

    @Override
    public MessageResponse envoyer(Long moi, Long correspondance, String contenu) {
        Correspondance c = correspondances.acces(moi, correspondance);
        Utilisateur destinataire = c.autre(moi);
        if (!c.estOuverte() || blocages.entre(moi, destinataire.getId())) {
            throw new RegleMetierException("erreur.conversation.fermee");
        }
        if (!limiteur.autoriser("message:" + moi, MESSAGES_PAR_MINUTE, Duration.ofMinutes(1))) {
            throw new TropDeDemandesException("erreur.messages.trop");
        }
        Utilisateur auteur = utilisateurs.getReferenceById(moi);
        MessageResponse envoye = mapper.reponse(messages.save(new Message(c, auteur, contenu.strip())));

        // une seule notification « message » non lue par conversation, pour ne pas inonder la cloche
        boolean dejaSignale = notificationsEnBase.findByDestinataireIdOrderByIdDesc(destinataire.getId(), PageRequest.of(0, 20))
            .stream().anyMatch(n -> !n.isLue() && n.getType() == TypeNotification.MESSAGE
                && n.getCorrespondance() != null && n.getCorrespondance().getId().equals(c.getId()));
        if (!dejaSignale) {
            notifications.notifier(destinataire, TypeNotification.MESSAGE, c);
        }
        NotificationServiceImpl.apresValidation(() -> {
            tempsReel.envoyer(destinataire.getId(), "MESSAGE", envoye);
            tempsReel.envoyer(moi, "MESSAGE", envoye);
        });
        return envoye;
    }
}
