package com.esmia.lovematch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.esmia.lovematch.Membres.Membre;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.StatutCompte;
import com.esmia.lovematch.repository.InteretRepository;
import com.esmia.lovematch.repository.UtilisateurRepository;
import com.esmia.lovematch.repository.VilleRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Critères d'acceptation 8.1 à 8.3 du cahier des charges, et règles de sécurité
@SpringBootTest(properties = "lovematch.demo=false")
@AutoConfigureMockMvc
class ExigencesTest {

    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired VilleRepository villes;
    @Autowired InteretRepository interets;
    @Autowired UtilisateurRepository utilisateurs;

    Membres membres;

    @BeforeEach
    void preparer() {
        membres = new Membres(mvc, json, villes, interets);
    }

    @Test
    @DisplayName("8.1 — on ne voit jamais une personne dont le genre, l'âge ou la distance vous exclut")
    void suggestionsRespectentLesPreferencesMutuelles() throws Exception {
        Membre fanja = membres.complet("FEMME", "HOMMES", 26, "Antsirabe");
        membres.preferences(fanja, "HOMMES", 22, 32, 10);

        Membre compatible = membres.complet("HOMME", "FEMMES", 28, "Antsirabe");
        Membre chercheHommes = membres.complet("HOMME", "HOMMES", 28, "Antsirabe");
        Membre tropAge = membres.complet("HOMME", "FEMMES", 45, "Antsirabe");
        Membre laTrouveTropAgee = membres.complet("HOMME", "FEMMES", 24, "Antsirabe");
        membres.preferences(laTrouveTropAgee, "FEMMES", 18, 24, 200);
        Membre tropLoin = membres.complet("HOMME", "FEMMES", 27, "Toliara");
        Membre femme = membres.complet("FEMME", "TOUS", 27, "Antsirabe");
        Membre incomplet = membres.inscrire("HOMME", "FEMMES", 27, "Antsirabe");
        Membre sansLangueCommune = membres.complet("HOMME", "FEMMES", 27, "Antsirabe", "ZH");

        assertThat(membres.suggestions(fanja))
            .contains(compatible.id())
            .doesNotContain(chercheHommes.id(), tropAge.id(), laTrouveTropAgee.id(), tropLoin.id(), femme.id(),
                incomplet.id(), sansLangueCommune.id(), fanja.id());

        // et l'API refuse un like envoyé à la main à quelqu'un d'exclu
        membres.reagir(fanja, chercheHommes, "LIKE", 409);
        membres.reagir(fanja, laTrouveTropAgee, "LIKE", 409);
        membres.reagir(fanja, sansLangueCommune, "LIKE", 409);

        // la règle est symétrique : lui non plus ne la voit pas
        assertThat(membres.suggestions(laTrouveTropAgee)).doesNotContain(fanja.id());
        assertThat(membres.suggestions(compatible)).contains(fanja.id());
    }

    @Test
    @DisplayName("Un profil déjà liké ou passé ne revient pas dans les suggestions")
    void unProfilVuNeRevientPas() throws Exception {
        Membre a = membres.complet("FEMME", "HOMMES", 30, "Ambositra");
        membres.preferences(a, "HOMMES", 25, 35, 10);
        Membre b = membres.complet("HOMME", "FEMMES", 30, "Ambositra");
        Membre c = membres.complet("HOMME", "FEMMES", 31, "Ambositra");

        membres.reagir(a, b, "LIKE", 201);
        membres.reagir(a, c, "PASSE", 201);
        assertThat(membres.suggestions(a)).doesNotContain(b.id(), c.id());
        membres.reagir(a, b, "LIKE", 409);

        // « Revoir » annule un passe
        mvc.perform(delete("/api/reactions/passes/" + c.id()).with(org.springframework.security.test.web.servlet
                .request.SecurityMockMvcRequestPostProcessors.csrf()).cookie(a.session()))
            .andExpect(status().isNoContent());
        assertThat(membres.suggestions(a)).contains(c.id());
    }

    @Test
    @DisplayName("Un profil incomplet ne reçoit pas de suggestions")
    void profilIncomplet() throws Exception {
        Membre m = membres.inscrire("FEMME", "HOMMES", 25, "Antananarivo");
        mvc.perform(get("/api/suggestions").cookie(m.session())).andExpect(status().isConflict());
        assertThat(membres.lire(m, "/api/profil").get("profilComplet").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("8.2 — pas de conversation sans match, et plus de conversation après un blocage")
    void conversationSeulementEntreMatchs() throws Exception {
        Membre a = membres.complet("FEMME", "HOMMES", 27, "Fianarantsoa");
        Membre b = membres.complet("HOMME", "FEMMES", 29, "Fianarantsoa");
        Membre intrus = membres.complet("HOMME", "FEMMES", 29, "Fianarantsoa");

        assertThat(membres.reagir(a, b, "LIKE", 201).get("match").asBoolean()).isFalse();
        assertThat(membres.lire(a, "/api/matchs")).isEmpty();
        JsonNode retour = membres.reagir(b, a, "LIKE", 201);
        assertThat(retour.get("match").asBoolean()).isTrue();
        long conversation = retour.get("correspondanceId").asLong();

        membres.envoyer(post("/api/matchs/" + conversation + "/messages"), a, Map.of("contenu", "Salama !"), 201);
        assertThat(membres.lire(b, "/api/matchs/" + conversation + "/messages").get(0).get("contenu").asString())
            .isEqualTo("Salama !");

        // quelqu'un d'extérieur ne peut ni lire ni écrire : la conversation « n'existe pas » pour lui
        mvc.perform(get("/api/matchs/" + conversation + "/messages").cookie(intrus.session()))
            .andExpect(status().isNotFound());
        membres.envoyer(post("/api/matchs/" + conversation + "/messages"), intrus, Map.of("contenu", "coucou"), 404);

        // b bloque a : la conversation se ferme pour les deux
        membres.envoyer(post("/api/blocages/" + a.id()), b, Map.of(), 204);
        membres.envoyer(post("/api/matchs/" + conversation + "/messages"), a, Map.of("contenu", "Tu es là ?"), 409);
        membres.envoyer(post("/api/matchs/" + conversation + "/messages"), b, Map.of("contenu", "Au revoir"), 409);
        assertThat(membres.suggestions(a)).doesNotContain(b.id());
        mvc.perform(get("/api/membres/" + b.id()).cookie(a.session())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("8.3 — le nom de famille et l'e-mail n'apparaissent jamais chez les autres membres")
    void pasDeFuiteDeDonneesPersonnelles() throws Exception {
        Membre a = membres.complet("FEMME", "HOMMES", 25, "Toamasina");
        membres.preferences(a, "HOMMES", 20, 30, 10);
        Membre b = membres.complet("HOMME", "FEMMES", 26, "Toamasina");

        membres.reagir(b, a, "LIKE", 201);
        long conversation = membres.reagir(a, b, "LIKE", 201).get("correspondanceId").asLong();
        membres.envoyer(post("/api/matchs/" + conversation + "/messages"), b, Map.of("contenu", "Bonjour"), 201);

        for (String url : new String[] {"/api/suggestions?taille=50", "/api/membres/" + b.id(), "/api/matchs",
                                        "/api/matchs/" + conversation, "/api/matchs/" + conversation + "/messages",
                                        "/api/notifications"}) {
            String reponse = membres.brut(a, url);
            assertThat(reponse).as(url).doesNotContain(b.nom()).doesNotContain(b.email());
        }
        // ses propres données, elle, les voit
        assertThat(membres.brut(a, "/api/profil")).contains(a.nom()).contains(a.email());
    }

    @Test
    @DisplayName("Les erreurs de l'API sont rédigées dans la langue demandée (Accept-Language)")
    void erreursTraduites() throws Exception {
        String[][] attendus = {
            {"fr", "Connectez-vous pour continuer."}, {"en", "Sign in to continue."}, {"zh", "请登录后继续。"},
            {"ar", "سجّل الدخول للمتابعة."}, {"mg", "Midira aloha vao manohy."},
        };
        for (String[] a : attendus) {
            String corps = mvc.perform(get("/api/suggestions").header("Accept-Language", a[0]))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
            assertThat(json.readTree(corps).get("message").asString()).as(a[0]).isEqualTo(a[1]);
        }
        Membre m = membres.complet("FEMME", "HOMMES", 25, "Antananarivo");
        String refus = mvc.perform(post("/api/reactions").with(org.springframework.security.test.web.servlet.request
                .SecurityMockMvcRequestPostProcessors.csrf()).cookie(m.session()).header("Accept-Language", "es")
                .contentType(MediaType.APPLICATION_JSON).content("{\"cibleId\":" + m.id() + ",\"decision\":\"LIKE\"}"))
            .andExpect(status().isConflict()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(json.readTree(refus).get("message").asString()).isEqualTo("No puedes reaccionar a tu propio perfil.");
    }

    @Test
    @DisplayName("Villes du monde : recherche sans accents, les plus peuplées d'abord, pays renvoyé")
    void villesDuMonde() throws Exception {
        JsonNode paris = json.readTree(mvc.perform(get("/api/referentiels/villes").param("q", "Paris"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(paris.get(0).get("nom").asString()).isEqualTo("Paris");
        assertThat(paris.get(0).get("pays").asString()).isEqualTo("FR");

        JsonNode montreal = json.readTree(mvc.perform(get("/api/referentiels/villes").param("q", "montré"))
            .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(montreal.get(0).get("pays").asString()).isEqualTo("CA");

        JsonNode tana = json.readTree(mvc.perform(get("/api/referentiels/villes").param("q", "antan"))
            .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(tana.get(0).get("nom").asString()).isEqualTo("Antananarivo");
        assertThat(tana.get(0).get("region").asString()).isEqualTo("Analamanga");

        mvc.perform(get("/api/referentiels/villes").param("q", "")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Distance internationale : visible seulement quand les deux rayons couvrent la distance")
    void rencontresInternationales() throws Exception {
        Membre tana = membres.complet("FEMME", "HOMMES", 27, "Antananarivo");
        Membre paris = membres.complet("HOMME", "FEMMES", 28, "Paris|FR");
        membres.preferences(paris, "FEMMES", 20, 35, 20000);

        membres.preferences(tana, "HOMMES", 20, 35, 200);
        assertThat(membres.suggestions(tana)).doesNotContain(paris.id());
        membres.preferences(tana, "HOMMES", 20, 35, 20000);
        assertThat(membres.suggestions(tana)).contains(paris.id());
        assertThat(membres.suggestions(paris)).contains(tana.id());
    }

    @Test
    @DisplayName("Genre non binaire : proposé seulement à qui cherche « tout le monde »")
    void genreNonBinaire() throws Exception {
        Membre nb = membres.complet("NON_BINAIRE", "TOUS", 26, "Mahajanga");
        Membre toutLeMonde = membres.complet("FEMME", "TOUS", 26, "Mahajanga");
        Membre hommes = membres.complet("FEMME", "HOMMES", 26, "Mahajanga");
        assertThat(membres.suggestions(toutLeMonde)).contains(nb.id());
        assertThat(membres.suggestions(hommes)).doesNotContain(nb.id());
        assertThat(membres.suggestions(nb)).contains(toutLeMonde.id()).doesNotContain(hommes.id());
    }

    @Test
    @DisplayName("Les dates partent avec leur fuseau horaire, pour être lues à l'heure de chacun")
    void datesAvecFuseau() throws Exception {
        Membre a = membres.complet("FEMME", "HOMMES", 30, "Morondava");
        Membre b = membres.complet("HOMME", "FEMMES", 30, "Morondava");
        membres.reagir(a, b, "LIKE", 201);
        long conversation = membres.reagir(b, a, "LIKE", 201).get("correspondanceId").asLong();
        JsonNode message = membres.envoyer(post("/api/matchs/" + conversation + "/messages"), a, Map.of("contenu", "Salama"), 201);
        assertThat(message.get("envoyeLe").asString()).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T.*[+]03:00");
    }

    @Test
    @DisplayName("Export RGPD : toutes mes données en JSON, jamais le nom de famille ni l'e-mail des autres")
    void exportDesDonnees() throws Exception {
        Membre a = membres.complet("FEMME", "HOMMES", 29, "Ambatondrazaka");
        Membre b = membres.complet("HOMME", "FEMMES", 29, "Ambatondrazaka");
        membres.reagir(a, b, "LIKE", 201);
        long conversation = membres.reagir(b, a, "LIKE", 201).get("correspondanceId").asLong();
        membres.envoyer(post("/api/matchs/" + conversation + "/messages"), b, Map.of("contenu", "Message pour l'export"), 201);

        var reponse = mvc.perform(get("/api/profil/export").cookie(a.session()))
            .andExpect(status().isOk()).andReturn().getResponse();
        assertThat(reponse.getHeader("Content-Disposition")).contains("attachment").contains("lovematch-mes-donnees.json");
        String corps = reponse.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode export = json.readTree(corps);
        assertThat(export.get("profil").get("email").asString()).isEqualTo(a.email());
        assertThat(export.get("reactions").size()).isEqualTo(1);
        assertThat(export.get("matchs").get(0).get("messages").get(0).get("contenu").asString()).isEqualTo("Message pour l'export");
        assertThat(export.get("matchs").get(0).get("messages").get(0).get("deMoi").asBoolean()).isFalse();
        assertThat(corps).doesNotContain(b.nom()).doesNotContain(b.email());
        mvc.perform(get("/api/profil/export")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Santé du service accessible sans session, sans détail")
    void sante() throws Exception {
        String corps = mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(corps).get("status").asString()).isEqualTo("UP");
        assertThat(corps).doesNotContain("db").doesNotContain("diskSpace");
        mvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Adresse inconnue : l'interface pour une page, du JSON pour l'API, toujours 404")
    void adressesInconnues() throws Exception {
        mvc.perform(get("/une-page-qui-n-existe-pas")).andExpect(status().isNotFound())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl("/index.html"));
        // sans session, l'API ne dit même pas si l'adresse existe
        mvc.perform(get("/api/n-existe-pas")).andExpect(status().isUnauthorized());
        Membre m = membres.complet("FEMME", "HOMMES", 25, "Antananarivo");
        mvc.perform(get("/api/n-existe-pas").cookie(m.session()).header("Accept-Language", "en")).andExpect(status().isNotFound())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message").value("Unknown address."));
    }

    @Test
    @DisplayName("Inscription réservée aux majeurs ; e-mail unique")
    void inscription() throws Exception {
        String corps = """
            {"email":"jeune@test.mg","motDePasse":"motdepasse-solide","prenom":"Jeune","nom":"Test",
             "dateNaissance":"%s","genre":"FEMME","villeId":%d,"genreRecherche":"HOMMES"}
            """.formatted(com.esmia.lovematch.util.Horloge.aujourdhui().minusYears(17), membres.ville("Antananarivo"));
        mvc.perform(post("/api/auth/inscription").with(org.springframework.security.test.web.servlet.request
                .SecurityMockMvcRequestPostProcessors.csrf()).contentType(MediaType.APPLICATION_JSON).content(corps))
            .andExpect(status().isConflict());

        Membre m = membres.inscrire("HOMME", "FEMMES", 22, "Antananarivo");
        String doublon = """
            {"email":"%s","motDePasse":"motdepasse-solide","prenom":"Autre","nom":"Test",
             "dateNaissance":"1995-01-01","genre":"HOMME","villeId":%d,"genreRecherche":"FEMMES"}
            """.formatted(m.email().toUpperCase(), membres.ville("Antananarivo"));
        mvc.perform(post("/api/auth/inscription").with(org.springframework.security.test.web.servlet.request
                .SecurityMockMvcRequestPostProcessors.csrf()).contentType(MediaType.APPLICATION_JSON).content(doublon))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Sécurité : session obligatoire, jeton CSRF exigé, compte suspendu déconnecté, admin réservé")
    void securite() throws Exception {
        mvc.perform(get("/api/suggestions")).andExpect(status().isUnauthorized());

        Membre m = membres.complet("FEMME", "HOMMES", 24, "Mahajanga");
        mvc.perform(post("/api/notifications/lues").cookie(m.session())).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/statistiques").cookie(m.session())).andExpect(status().isForbidden());

        Utilisateur u = utilisateurs.findById(m.id()).orElseThrow();
        u.setStatut(StatutCompte.SUSPENDU);
        utilisateurs.save(u);
        mvc.perform(get("/api/profil").cookie(m.session())).andExpect(status().isUnauthorized());
    }
}
