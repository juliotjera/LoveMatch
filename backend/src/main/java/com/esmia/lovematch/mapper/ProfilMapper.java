package com.esmia.lovematch.mapper;

import com.esmia.lovematch.dto.response.InteretResponse;
import com.esmia.lovematch.dto.response.MoiResponse;
import com.esmia.lovematch.dto.response.ProfilPublicResponse;
import com.esmia.lovematch.dto.response.VilleResponse;
import com.esmia.lovematch.entity.Interet;
import com.esmia.lovematch.entity.Photo;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.entity.Ville;
import com.esmia.lovematch.enumeration.Langue;
import com.esmia.lovematch.repository.PhotoRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ProfilMapper {

    private final PhotoRepository photos;

    public ProfilMapper(PhotoRepository photos) {
        this.photos = photos;
    }

    public MoiResponse moi(Utilisateur u) {
        return new MoiResponse(u.getId(), u.getEmail(), u.getPrenom(), u.getNom(), u.getDateNaissance(), u.age(), u.getGenre(),
            ville(u.getVille()), u.getBio(), u.getRecherche(), interets(u), langues(u), photosDe(u.getId()),
            u.getGenreRecherche(), u.getAgeMin(), u.getAgeMax(), u.getRayonKm(), u.getRole(), u.isDemo(), estComplet(u));
    }

    public ProfilPublicResponse publicDe(Utilisateur u) {
        return publicDe(u, photosDe(u.getId()));
    }

    public ProfilPublicResponse publicDe(Utilisateur u, List<Long> photoIds) {
        return new ProfilPublicResponse(u.getId(), u.getPrenom(), u.age(), u.getGenre(), u.getVille().getNom(),
            u.getVille().getRegion(), u.getBio(), u.getRecherche(), interets(u), langues(u), photoIds);
    }

    // identifiants des photos de plusieurs membres en une requête (listes de suggestions, de matchs)
    public Map<Long, List<Long>> photosDe(Collection<Long> membres) {
        Map<Long, List<Long>> parMembre = new HashMap<>();
        if (!membres.isEmpty()) {
            for (Object[] ligne : photos.identifiantsParMembre(membres)) {
                parMembre.computeIfAbsent((Long) ligne[0], k -> new ArrayList<>()).add((Long) ligne[1]);
            }
        }
        return parMembre;
    }

    public List<Long> photosDe(Long membre) {
        return photos.findByUtilisateurIdOrderByOrdreAsc(membre).stream().map(Photo::getId).toList();
    }

    public VilleResponse ville(Ville v) {
        return new VilleResponse(v.getId(), v.getNom(), v.getRegion());
    }

    public InteretResponse interet(Interet i) {
        return new InteretResponse(i.getId(), i.getCode(), i.getLibelleFr(), i.getLibelleMg());
    }

    private List<Langue> langues(Utilisateur u) {
        return u.getLangues().stream().sorted().toList();
    }

    private List<InteretResponse> interets(Utilisateur u) {
        return u.getInterets().stream().sorted(Comparator.comparing(Interet::getLibelleFr)).map(this::interet).toList();
    }

    // profil visible des autres : une bio et au moins trois centres d'intérêt
    public static boolean estComplet(Utilisateur u) {
        return u.getBio() != null && !u.getBio().isBlank() && u.getInterets().size() >= 3;
    }
}
