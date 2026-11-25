package com.esmia.lovematch;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.esmia.lovematch.entity.Interet;
import com.esmia.lovematch.repository.InteretRepository;
import com.esmia.lovematch.repository.VilleRepository;
import com.esmia.lovematch.util.Horloge;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Crée des membres de test par l'API, comme le ferait l'interface
final class Membres {

    record Membre(Long id, String email, String nom, Cookie session) {}

    private static final AtomicInteger COMPTEUR = new AtomicInteger();

    private final MockMvc mvc;
    private final JsonMapper json;
    private final VilleRepository villes;
    private final List<Interet> interets;

    Membres(MockMvc mvc, JsonMapper json, VilleRepository villes, InteretRepository interets) {
        this.mvc = mvc;
        this.json = json;
        this.villes = villes;
        this.interets = interets.findAllByOrderByLibelleFrAsc();
    }

    Long ville(String nom) {
        String[] morceaux = nom.split("\\|");
        return villes.findFirstByNomAsciiAndPaysOrderByPopulationDesc(
            db.migration.V4__Villes_du_monde.normaliser(morceaux[0]), morceaux.length > 1 ? morceaux[1] : "MG").orElseThrow().getId();
    }

    // inscription puis profil complété (bio et trois centres d'intérêt) : le membre est visible
    Membre complet(String genre, String cherche, int age, String ville) throws Exception {
        return complet(genre, cherche, age, ville, "FR");
    }

    // langues : codes parlés par le membre (« FR », « ZH »…)
    Membre complet(String genre, String cherche, int age, String ville, String... langues) throws Exception {
        Membre m = inscrire(genre, cherche, age, ville);
        envoyer(put("/api/profil"), m, Map.of(
            "prenom", "Test", "nom", m.nom(), "villeId", ville(ville), "bio", "Bio de test",
            "recherche", "A_DECOUVRIR",
            "interets", List.of(interets.get(0).getId(), interets.get(1).getId(), interets.get(2).getId()),
            "langues", List.of(langues)), 200);
        return m;
    }

    Membre inscrire(String genre, String cherche, int age, String ville) throws Exception {
        int n = COMPTEUR.incrementAndGet();
        // nom de famille et e-mail uniques : on vérifie qu'ils ne fuient dans aucune réponse destinée aux autres
        String nom = "Secretnom" + UUID.randomUUID().toString().substring(0, 8);
        String email = "membre" + n + "-" + UUID.randomUUID().toString().substring(0, 6) + "@test.mg";
        MvcResult r = mvc.perform(post("/api/auth/inscription").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                    "email", email, "motDePasse", "motdepasse-solide", "prenom", "Test", "nom", nom,
                    "dateNaissance", Horloge.aujourdhui().minusYears(age).minusDays(10).toString(),
                    "genre", genre, "villeId", ville(ville), "genreRecherche", cherche))))
            .andExpect(status().isCreated())
            .andReturn();
        Long id = json.readTree(r.getResponse().getContentAsString()).get("id").asLong();
        return new Membre(id, email, nom, r.getResponse().getCookie("LM_SESSION"));
    }

    void preferences(Membre m, String cherche, int ageMin, int ageMax, int rayonKm) throws Exception {
        envoyer(put("/api/profil/preferences"), m,
            Map.of("genreRecherche", cherche, "ageMin", ageMin, "ageMax", ageMax, "rayonKm", rayonKm), 200);
    }

    JsonNode reagir(Membre auteur, Membre cible, String decision, int attendu) throws Exception {
        return envoyer(post("/api/reactions"), auteur, Map.of("cibleId", cible.id(), "decision", decision), attendu);
    }

    List<Long> suggestions(Membre m) throws Exception {
        JsonNode page = lire(m, "/api/suggestions?taille=50");
        List<Long> ids = new java.util.ArrayList<>();
        for (JsonNode s : page.get("elements")) {
            ids.add(s.get("profil").get("id").asLong());
        }
        return ids;
    }

    String brut(Membre m, String url) throws Exception {
        return mvc.perform(get(url).cookie(m.session())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    JsonNode lire(Membre m, String url) throws Exception {
        return json.readTree(brut(m, url));
    }

    JsonNode envoyer(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requete, Membre m,
                     Object corps, int attendu) throws Exception {
        String reponse = mvc.perform(requete.with(csrf()).cookie(m.session())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corps)))
            .andExpect(status().is(attendu))
            .andReturn().getResponse().getContentAsString();
        return reponse.isEmpty() ? null : json.readTree(reponse);
    }
}
