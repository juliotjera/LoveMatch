package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.dto.response.PageResponse;
import com.esmia.lovematch.dto.response.ScoreResponse;
import com.esmia.lovematch.dto.response.SuggestionResponse;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.Genre;
import com.esmia.lovematch.enumeration.GenreRecherche;
import com.esmia.lovematch.exception.RegleMetierException;
import com.esmia.lovematch.exception.RessourceIntrouvableException;
import com.esmia.lovematch.mapper.ProfilMapper;
import com.esmia.lovematch.repository.UtilisateurRepository;
import com.esmia.lovematch.service.SuggestionService;
import com.esmia.lovematch.util.CalculScore;
import com.esmia.lovematch.util.Horloge;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Suggestions triées par score décroissant (sujet d'origine), puis par activité récente
@Service
@Transactional
public class SuggestionServiceImpl implements SuggestionService {

    private record Candidat(Utilisateur membre, CalculScore.Detail score) {}

    private final UtilisateurRepository utilisateurs;
    private final ProfilMapper mapper;

    public SuggestionServiceImpl(UtilisateurRepository utilisateurs, ProfilMapper mapper) {
        this.utilisateurs = utilisateurs;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<SuggestionResponse> suggestions(Long moi, int page, int taille, Integer distanceMax, boolean interetsCommuns) {
        Utilisateur u = utilisateurs.findById(moi).orElseThrow(() -> new RessourceIntrouvableException("erreur.compte.introuvable"));
        if (!ProfilMapper.estComplet(u)) {
            throw new RegleMetierException("erreur.profil.incomplet");
        }
        u.setDerniereActivite(Horloge.maintenant());

        List<Genre> genres = Arrays.stream(Genre.values()).filter(g -> u.getGenreRecherche().accepte(g)).toList();
        List<GenreRecherche> quiMAcceptent = Arrays.stream(GenreRecherche.values()).filter(g -> g.accepte(u.getGenre())).toList();
        LocalDate aujourdhui = Horloge.aujourdhui();
        LocalDate neAuPlusTot = aujourdhui.minusYears(u.getAgeMax() + 1L).plusDays(1);
        LocalDate neAuPlusTard = aujourdhui.minusYears(u.getAgeMin());

        List<Candidat> tries = utilisateurs.candidats(moi, genres, quiMAcceptent, neAuPlusTot, neAuPlusTard).stream()
            .filter(c -> c.getInterets().size() >= 3)
            .filter(c -> CalculScore.compatibles(u, c))
            .map(c -> new Candidat(c, CalculScore.calculer(u, c)))
            .filter(c -> distanceMax == null || c.score().distanceKm() <= distanceMax)
            .filter(c -> !interetsCommuns || !c.score().interetsCommuns().isEmpty())
            .sorted(Comparator.comparingInt((Candidat c) -> c.score().total()).reversed()
                .thenComparing(c -> c.membre().getDerniereActivite(), Comparator.reverseOrder()))
            .toList();

        int debut = Math.min(page * taille, tries.size());
        List<Candidat> pageCandidats = tries.subList(debut, Math.min(debut + taille, tries.size()));
        Map<Long, List<Long>> photos = mapper.photosDe(pageCandidats.stream().map(c -> c.membre().getId()).toList());
        List<SuggestionResponse> elements = pageCandidats.stream()
            .map(c -> new SuggestionResponse(
                mapper.publicDe(c.membre(), photos.getOrDefault(c.membre().getId(), List.of())),
                score(c.score())))
            .toList();
        return new PageResponse<>(elements, page, taille, tries.size());
    }

    static ScoreResponse score(CalculScore.Detail d) {
        return new ScoreResponse(d.total(), d.age(), d.distance(), d.interets(), d.langues(), d.recherche(), d.distanceKm(),
            d.interetsCommuns(), d.languesCommunes());
    }
}
