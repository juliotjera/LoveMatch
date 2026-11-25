package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.entity.Photo;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.exception.RegleMetierException;
import com.esmia.lovematch.exception.RessourceIntrouvableException;
import com.esmia.lovematch.repository.BlocageRepository;
import com.esmia.lovematch.repository.PhotoRepository;
import com.esmia.lovematch.repository.UtilisateurRepository;
import com.esmia.lovematch.service.PhotoService;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class PhotoServiceImpl implements PhotoService {

    public static final int MAXIMUM = 4;
    private static final int COTE_MAX = 960;

    private final PhotoRepository photos;
    private final UtilisateurRepository utilisateurs;
    private final BlocageRepository blocages;

    public PhotoServiceImpl(PhotoRepository photos, UtilisateurRepository utilisateurs, BlocageRepository blocages) {
        this.photos = photos;
        this.utilisateurs = utilisateurs;
        this.blocages = blocages;
    }

    @Override
    public Long ajouter(Long moi, MultipartFile fichier) {
        long deja = photos.countByUtilisateurId(moi);
        if (deja >= MAXIMUM) {
            throw new RegleMetierException("erreur.photos.maximum");
        }
        Utilisateur u = utilisateurs.getReferenceById(moi);
        return photos.save(new Photo(u, allegee(fichier), "image/jpeg", (int) deja)).getId();
    }

    @Override
    public void supprimer(Long moi, Long photo) {
        Photo p = sienne(moi, photo);
        photos.delete(p);
        photos.flush();
        renumeroter(photos.findByUtilisateurIdOrderByOrdreAsc(moi));
    }

    @Override
    public void definirPrincipale(Long moi, Long photo) {
        Photo choisie = sienne(moi, photo);
        List<Photo> toutes = new java.util.ArrayList<>(photos.findByUtilisateurIdOrderByOrdreAsc(moi));
        toutes.remove(choisie);
        toutes.addFirst(choisie);
        renumeroter(toutes);
    }

    @Override
    @Transactional(readOnly = true)
    public Photo lire(Long moi, Long photo) {
        Photo p = photos.findById(photo).orElseThrow(() -> new RessourceIntrouvableException("erreur.photo.introuvable"));
        Long proprietaire = p.getUtilisateur().getId();
        if (!proprietaire.equals(moi) && (!p.getUtilisateur().estActif() || blocages.entre(moi, proprietaire))) {
            throw new RessourceIntrouvableException("erreur.photo.introuvable");
        }
        return p;
    }

    private Photo sienne(Long moi, Long photo) {
        return photos.findById(photo).filter(p -> p.getUtilisateur().getId().equals(moi))
            .orElseThrow(() -> new RessourceIntrouvableException("erreur.photo.introuvable"));
    }

    private static void renumeroter(List<Photo> ordonnees) {
        for (int i = 0; i < ordonnees.size(); i++) {
            ordonnees.get(i).setOrdre(i);
        }
    }

    // vraie image, recadrée à 960 px au plus et recompressée : photos légères pour un réseau lent
    static byte[] allegee(MultipartFile fichier) {
        BufferedImage source;
        try (InputStream entree = fichier.getInputStream()) {
            source = ImageIO.read(entree);
        } catch (IOException e) {
            source = null;
        }
        if (source == null) {
            throw new RegleMetierException("erreur.photo.format");
        }
        double echelle = Math.min(1, COTE_MAX / (double) Math.max(source.getWidth(), source.getHeight()));
        int largeur = Math.max(1, (int) Math.round(source.getWidth() * echelle));
        int hauteur = Math.max(1, (int) Math.round(source.getHeight() * echelle));
        BufferedImage cible = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = cible.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, largeur, hauteur);
        g.drawImage(source, 0, 0, largeur, hauteur, null);
        g.dispose();
        return jpeg(cible, 0.82f);
    }

    public static byte[] jpeg(BufferedImage image, float qualite) {
        try (ByteArrayOutputStream sortie = new ByteArrayOutputStream();
             ImageOutputStream flux = ImageIO.createImageOutputStream(sortie)) {
            ImageWriter ecrivain = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam parametres = ecrivain.getDefaultWriteParam();
            parametres.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parametres.setCompressionQuality(qualite);
            ecrivain.setOutput(flux);
            ecrivain.write(null, new IIOImage(image, null, null), parametres);
            ecrivain.dispose();
            flux.flush();
            return sortie.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Compression de l'image impossible", e);
        }
    }
}
