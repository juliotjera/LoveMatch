package com.esmia.lovematch.service;

import com.esmia.lovematch.dto.response.NotificationResponse;
import com.esmia.lovematch.entity.Correspondance;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.TypeNotification;
import java.util.List;

public interface NotificationService {

    void notifier(Utilisateur destinataire, TypeNotification type, Correspondance correspondance);

    List<NotificationResponse> recentes(Long moi);

    long nonLues(Long moi);

    void toutLire(Long moi);
}
