package com.esmia.lovematch.service;

import com.esmia.lovematch.dto.response.MessageResponse;
import java.util.List;

public interface MessagerieService {

    List<MessageResponse> historique(Long moi, Long correspondance, Long avant, int taille);

    MessageResponse envoyer(Long moi, Long correspondance, String contenu);
}
