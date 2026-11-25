package com.esmia.lovematch.service;

import com.esmia.lovematch.entity.Photo;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {

    Long ajouter(Long moi, MultipartFile fichier);

    void supprimer(Long moi, Long photo);

    void definirPrincipale(Long moi, Long photo);

    Photo lire(Long moi, Long photo);
}
