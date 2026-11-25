package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.config.TempsReel;
import com.esmia.lovematch.dto.response.NotificationResponse;
import com.esmia.lovematch.entity.Correspondance;
import com.esmia.lovematch.entity.Notification;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.TypeNotification;
import com.esmia.lovematch.repository.NotificationRepository;
import com.esmia.lovematch.service.NotificationService;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

// Notification enregistrée (cloche) et poussée en temps réel, une fois la transaction validée
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notifications;
    private final TempsReel tempsReel;

    public NotificationServiceImpl(NotificationRepository notifications, TempsReel tempsReel) {
        this.notifications = notifications;
        this.tempsReel = tempsReel;
    }

    @Override
    public void notifier(Utilisateur destinataire, TypeNotification type, Correspondance correspondance) {
        Notification n = notifications.save(new Notification(destinataire, type, correspondance));
        NotificationResponse reponse = reponse(n);
        apresValidation(() -> tempsReel.envoyer(destinataire.getId(), "NOTIFICATION", reponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> recentes(Long moi) {
        return notifications.findByDestinataireIdOrderByIdDesc(moi, PageRequest.of(0, 40)).stream().map(this::reponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long nonLues(Long moi) {
        return notifications.countByDestinataireIdAndLueFalse(moi);
    }

    @Override
    public void toutLire(Long moi) {
        notifications.toutMarquerLu(moi);
    }

    private NotificationResponse reponse(Notification n) {
        Correspondance c = n.getCorrespondance();
        // un like reste anonyme : aucun prénom
        String prenom = c == null ? null : c.autre(n.getDestinataire().getId()).getPrenom();
        return new NotificationResponse(n.getId(), n.getType(), c == null ? null : c.getId(), prenom, n.isLue(), n.getCreeLe());
    }

    static void apresValidation(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
