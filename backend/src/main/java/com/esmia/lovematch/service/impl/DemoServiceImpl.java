package com.esmia.lovematch.service.impl;

import com.esmia.lovematch.entity.Correspondance;
import com.esmia.lovematch.entity.Message;
import com.esmia.lovematch.entity.Notification;
import com.esmia.lovematch.entity.Photo;
import com.esmia.lovematch.entity.Reaction;
import com.esmia.lovematch.entity.Signalement;
import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.entity.Ville;
import com.esmia.lovematch.enumeration.Decision;
import com.esmia.lovematch.enumeration.Genre;
import com.esmia.lovematch.enumeration.GenreRecherche;
import com.esmia.lovematch.enumeration.Langue;
import com.esmia.lovematch.enumeration.MotifSignalement;
import com.esmia.lovematch.enumeration.Recherche;
import com.esmia.lovematch.enumeration.Role;
import com.esmia.lovematch.enumeration.TypeNotification;
import com.esmia.lovematch.repository.CorrespondanceRepository;
import com.esmia.lovematch.repository.InteretRepository;
import com.esmia.lovematch.repository.MessageRepository;
import com.esmia.lovematch.repository.NotificationRepository;
import com.esmia.lovematch.repository.PhotoRepository;
import com.esmia.lovematch.repository.ReactionRepository;
import com.esmia.lovematch.repository.SignalementRepository;
import com.esmia.lovematch.repository.UtilisateurRepository;
import com.esmia.lovematch.repository.VilleRepository;
import com.esmia.lovematch.service.DemoService;
import com.esmia.lovematch.util.Horloge;
import com.esmia.lovematch.util.Illustration;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Jeu de démonstration (« Essayer sans compte ») : des membres fictifs dans plusieurs villes de Madagascar,
 * avec des portraits dessinés. Le compte membre est Fanja ; le compte de modération voit un signalement ouvert.
 */
@Service
public class DemoServiceImpl implements DemoService {

    private static final Logger LOG = LoggerFactory.getLogger(DemoServiceImpl.class);

    // comptes ouverts par « Essayer sans compte »
    public static final String EMAIL_MEMBRE = "fanja.demo@lovematch.mg";
    public static final String EMAIL_MODERATION = "moderation.demo@lovematch.mg";

    private record Fiche(String prenom, String nom, Genre genre, int age, String ville, Recherche recherche,
                         GenreRecherche cherche, int ageMin, int ageMax, int rayonKm, String bio, String interets) {}

    private static final Fiche FANJA = new Fiche("Fanja", "Rasoanaivo", Genre.FEMME, 26, "Antananarivo",
        Recherche.RELATION_SERIEUSE, GenreRecherche.HOMMES, 23, 34, 200,
        "Infirmière à Tana, je cuisine pour mes amis le week-end et je photographie tout ce qui bouge. "
            + "Je cherche quelqu'un de simple, curieux, qui aime marcher.",
        "musique voyage cuisine lecture randonnee photo");

    private static final List<Fiche> MEMBRES = List.of(
        new Fiche("Tiana", "Rakotomalala", Genre.HOMME, 28, "Antananarivo", Recherche.RELATION_SERIEUSE, GenreRecherche.FEMMES, 22, 32, 100,
            "Comptable le jour, guitariste le soir. Je connais toutes les gargotes d'Analakely.", "musique voyage cuisine football photo"),
        new Fiche("Hery", "Andrianarisoa", Genre.HOMME, 30, "Ivato", Recherche.RELATION_SERIEUSE, GenreRecherche.FEMMES, 24, 33, 150,
            "Guide de randonnée. Le dimanche, on me trouve plutôt à l'Andringitra qu'en ville.", "randonnee nature photo voyage animaux"),
        new Fiche("Mamy", "Razafindrakoto", Genre.HOMME, 27, "Antsirabe", Recherche.RELATION_SERIEUSE, GenreRecherche.FEMMES, 22, 31, 200,
            "Cuisinier dans un hôtel d'Antsirabe. J'aime les bons livres et les longues discussions.", "cuisine lecture cinema musique"),
        new Fiche("Andry", "Ratsimbazafy", Genre.HOMME, 25, "Ambohidratrimo", Recherche.A_DECOUVRIR, GenreRecherche.FEMMES, 20, 29, 80,
            "Développeur, fan de foot et de jeux vidéo. Je fais aussi un excellent mofo gasy.", "football jeux_video informatique musique"),
        new Fiche("Rado", "Rabemananjara", Genre.HOMME, 31, "Ambatolampy", Recherche.RELATION_SERIEUSE, GenreRecherche.FEMMES, 25, 33, 120,
            "J'ai monté ma petite entreprise d'artisanat. Je parle anglais, je prends des cours de chinois.", "entrepreneuriat lecture voyage langues"),
        new Fiche("Toky", "Randriamampionona", Genre.HOMME, 29, "Arivonimamo", Recherche.A_DECOUVRIR, GenreRecherche.FEMMES, 23, 32, 120,
            "Musicien de hira gasy, je danse dès qu'il y a du salegy.", "danse musique chant culture"),
        new Fiche("Fidy", "Rakotondrabe", Genre.HOMME, 33, "Moramanga", Recherche.AMITIE, GenreRecherche.FEMMES, 25, 36, 150,
            "Vétérinaire. Je passe mes congés à Andasibe pour écouter les indris.", "nature animaux randonnee photo"),
        new Fiche("Njaka", "Rajaonarison", Genre.HOMME, 24, "Betafo", Recherche.A_DECOUVRIR, GenreRecherche.FEMMES, 20, 28, 150,
            "Basket, salle de sport et séries coréennes. Oui, les trois.", "basket fitness series"),
        new Fiche("Haja", "Ramanantsoa", Genre.HOMME, 32, "Miarinarivo", Recherche.RELATION_SERIEUSE, GenreRecherche.FEMMES, 25, 34, 150,
            "Bénévole à la Croix-Rouge, chef de chœur à l'église, et jamais en retard.", "cuisine benevolat spiritualite chant"),
        new Fiche("Lova", "Rasolofonirina", Genre.HOMME, 26, "Ambositra", Recherche.RELATION_SERIEUSE, GenreRecherche.FEMMES, 22, 30, 250,
            "Sculpteur sur bois à Ambositra. Je vous emmène voir les ateliers zafimaniry ?", "art photo culture voyage"),
        new Fiche("Mahery", "Rakotoarivelo", Genre.HOMME, 28, "Toamasina", Recherche.RELATION_SERIEUSE, GenreRecherche.FEMMES, 22, 32, 100,
            "Docker au port de Toamasina, plongeur le dimanche.", "plage voyage musique football"),
        new Fiche("Sitraka", "Andriamihaja", Genre.HOMME, 36, "Fianarantsoa", Recherche.AMITIE, GenreRecherche.FEMMES, 28, 40, 150,
            "Professeur de français, amateur de vin d'Ambalavao.", "lecture langues culture cinema"),
        new Fiche("Ony", "Raharimalala", Genre.FEMME, 24, "Antananarivo", Recherche.A_DECOUVRIR, GenreRecherche.HOMMES, 22, 30, 100,
            "Étudiante en droit, je chante dans une chorale et je ne dis jamais non à une sortie ciné.", "chant cinema series mode"),
        new Fiche("Voahirana", "Rabearivelo", Genre.FEMME, 29, "Toamasina", Recherche.RELATION_SERIEUSE, GenreRecherche.HOMMES, 26, 35, 150,
            "Sage-femme à Toamasina. La mer, les livres, et le riz au coco de ma mère.", "plage lecture cuisine benevolat"),
        new Fiche("Mialy", "Rakotonirina", Genre.FEMME, 27, "Mahajanga", Recherche.AMITIE, GenreRecherche.TOUS, 22, 35, 200,
            "Je viens d'arriver à Majunga pour le travail, je cherche des gens pour découvrir la ville.", "voyage plage danse photo"),
        new Fiche("Hanitra", "Ravelojaona", Genre.FEMME, 31, "Antsirabe", Recherche.RELATION_SERIEUSE, GenreRecherche.HOMMES, 28, 38, 120,
            "Ingénieure agronome. Je cultive des fraises sur mon balcon, avec un succès relatif.", "nature cuisine entrepreneuriat lecture"),
        new Fiche("Lalaina", "Rasoamanarivo", Genre.FEMME, 23, "Fianarantsoa", Recherche.A_DECOUVRIR, GenreRecherche.HOMMES, 21, 28, 100,
            "Étudiante en informatique à Fianar. Les jeux vidéo, oui, mais aussi la randonnée.", "informatique jeux_video randonnee musique"),
        new Fiche("Nirina", "Randrianasolo", Genre.FEMME, 34, "Nosy Be", Recherche.RELATION_SERIEUSE, GenreRecherche.HOMMES, 30, 42, 300,
            "Je tiens une maison d'hôtes à Nosy Be. Venez pour les couchers de soleil, restez pour le rhum arrangé.", "plage voyage cuisine langues"),
        new Fiche("Malala", "Razanadrakoto", Genre.FEMME, 26, "Antsiranana", Recherche.AMITIE, GenreRecherche.TOUS, 22, 32, 150,
            "Photographe à Diego. Je cherche des modèles et des compagnons de route.", "photo art voyage mode"),
        new Fiche("Rindra", "Rafanomezantsoa", Genre.FEMME, 28, "Morondava", Recherche.A_DECOUVRIR, GenreRecherche.HOMMES, 25, 34, 200,
            "Guide sur l'allée des baobabs. Je connais la meilleure heure pour la photo.", "nature photo voyage culture"),
        new Fiche("Wei", "Chen", Genre.HOMME, 29, "Antananarivo", Recherche.RELATION_SERIEUSE, GenreRecherche.FEMMES, 23, 33, 100,
            "Troisième génération d'une famille d'épiciers de Behoririka. Je parle chinois à la maison, malgache dans la rue.", "cuisine entrepreneuriat voyage photo"),
        new Fiche("Arjun", "Mehta", Genre.HOMME, 27, "Antananarivo", Recherche.A_DECOUVRIR, GenreRecherche.FEMMES, 22, 31, 150,
            "Ingénieur télécom arrivé de Pune il y a deux ans. Je cuisine un dal correct et j'apprends le malgache.", "informatique cuisine voyage randonnee"),
        new Fiche("Diego", "Navarro", Genre.HOMME, 30, "Antsirabe", Recherche.AMITIE, GenreRecherche.FEMMES, 24, 34, 200,
            "Spanish volunteer with a farming NGO in Antsirabe. Still learning French, salsa teacher on weekends.", "danse nature voyage benevolat"),
        new Fiche("Jun", "Liu", Genre.HOMME, 27, "Antananarivo", Recherche.A_DECOUVRIR, GenreRecherche.FEMMES, 22, 32, 100,
            "在塔那那利佛做进出口生意，周末喜欢爬山和拍照。", "randonnee photo voyage"),
        new Fiche("Yasmine", "Saïd", Genre.FEMME, 25, "Mahajanga", Recherche.RELATION_SERIEUSE, GenreRecherche.HOMMES, 23, 32, 150,
            "Pharmacienne à Majunga. J'aime la mer, la poésie et les longues soirées sur le boulevard.", "plage lecture culture cuisine"),
        // profil signalé pour la démonstration de la modération ; ne correspond pas aux critères de Fanja
        new Fiche("Rojo", "Ranaivoson", Genre.HOMME, 30, "Antananarivo", Recherche.A_DECOUVRIR, GenreRecherche.FEMMES, 18, 24, 300,
            "Homme d'affaires à l'étranger. Envoyez-moi votre numéro et un peu de crédit pour qu'on parle.", "voyage entrepreneuriat musique")
    );

    // langues parlées des membres fictifs (par défaut : malgache et français)
    private static final Map<String, String> LANGUES = Map.ofEntries(
        Map.entry("Fanja", "MG FR EN"), Map.entry("Rado", "MG FR EN ZH"), Map.entry("Nirina", "MG FR EN IT"),
        Map.entry("Malala", "MG FR EN"), Map.entry("Lalaina", "MG FR EN"), Map.entry("Hery", "MG FR EN"),
        Map.entry("Wei", "ZH MG FR"), Map.entry("Arjun", "HI EN FR"), Map.entry("Diego", "ES EN"),
        Map.entry("Jun", "ZH"), Map.entry("Yasmine", "AR FR MG"));

    // deuxième photo de certains membres : un vrai paysage de Madagascar (Wikimedia Commons, voir docs/CREDITS_PHOTOS.md)
    private static final Map<String, String> PAYSAGES = Map.of(
        "Fanja", "jacaranda-antananarivo", "Rindra", "baobabs-morondava", "Fidy", "andasibe", "Hery", "andringitra",
        "Lova", "isalo", "Hanitra", "rizieres", "Nirina", "nosy-iranja");

    private final UtilisateurRepository utilisateurs;
    private final VilleRepository villes;
    private final InteretRepository interets;
    private final PhotoRepository photos;
    private final ReactionRepository reactions;
    private final CorrespondanceRepository correspondances;
    private final MessageRepository messages;
    private final NotificationRepository notifications;
    private final SignalementRepository signalements;
    private final PasswordEncoder encodeur;

    public DemoServiceImpl(UtilisateurRepository utilisateurs, VilleRepository villes, InteretRepository interets,
                           PhotoRepository photos, ReactionRepository reactions, CorrespondanceRepository correspondances,
                           MessageRepository messages, NotificationRepository notifications,
                           SignalementRepository signalements, PasswordEncoder encodeur) {
        this.utilisateurs = utilisateurs;
        this.villes = villes;
        this.interets = interets;
        this.photos = photos;
        this.reactions = reactions;
        this.correspondances = correspondances;
        this.messages = messages;
        this.notifications = notifications;
        this.signalements = signalements;
        this.encodeur = encodeur;
    }

    @Override
    @Transactional
    public void preparer() {
        utilisateurs.supprimerComptesDemo();
        Map<String, Ville> parNom = villes.findAll().stream().collect(Collectors.toMap(Ville::getNom, Function.identity()));
        // personne ne se connecte à ces comptes par mot de passe : un mot de passe aléatoire, haché une fois
        String motDePasse = encodeur.encode(UUID.randomUUID().toString());

        Utilisateur fanja = creer(FANJA, parNom, motDePasse, 0);
        Map<String, Utilisateur> membres = new HashMap<>();
        for (int i = 0; i < MEMBRES.size(); i++) {
            Fiche f = MEMBRES.get(i);
            membres.put(f.prenom(), creer(f, parNom, motDePasse, i + 1));
        }

        Utilisateur moderation = new Utilisateur();
        moderation.setEmail(EMAIL_MODERATION);
        moderation.setMotDePasse(motDePasse);
        moderation.setPrenom("Équipe");
        moderation.setNom("Modération");
        moderation.setDateNaissance(Horloge.aujourdhui().minusYears(30));
        moderation.setGenre(Genre.FEMME);
        moderation.setVille(parNom.get("Antananarivo"));
        moderation.setGenreRecherche(GenreRecherche.TOUS);
        moderation.setRole(Role.ADMIN);
        moderation.setDemo(true);
        moderation.getLangues().add(Langue.FR);
        utilisateurs.save(moderation);

        // ils ont déjà liké Fanja : si elle les like à son tour, c'est un match immédiat
        for (String prenom : List.of("Tiana", "Hery", "Lova")) {
            reactions.save(new Reaction(membres.get(prenom), fanja, Decision.LIKE));
        }
        notifications.save(new Notification(fanja, TypeNotification.LIKE, null));

        LocalDateTime maintenant = Horloge.maintenant();
        Utilisateur mamy = membres.get("Mamy");
        Correspondance avecMamy = matcher(fanja, mamy);
        conversation(avecMamy, maintenant.minusDays(2).withHour(19).withMinute(4), List.of(
            new Object[] {mamy, "Salama Fanja ! On a tous les deux mis « cuisine » : tu as une spécialité ?"},
            new Object[] {fanja, "Salama ! Le romazava de ma grand-mère, sans hésiter. Et toi ?"},
            new Object[] {mamy, "Le koba akondro. J'en ramène toujours d'Antsirabe quand je monte à Tana."},
            new Object[] {fanja, "Alors il faudra me faire goûter ça un jour."},
            new Object[] {mamy, "Avec plaisir. Tu serais libre pour un café à Analakely samedi ?"}
        ), fanja);
        notifications.save(new Notification(fanja, TypeNotification.MESSAGE, avecMamy));

        Utilisateur toky = membres.get("Toky");
        Correspondance avecToky = matcher(fanja, toky);
        conversation(avecToky, maintenant.minusHours(3), List.<Object[]>of(
            new Object[] {toky, "Manao ahoana ! Tu aimes la musique : tu écoutes du salegy ou plutôt du vakodrazana ?"}
        ), fanja);
        notifications.save(new Notification(fanja, TypeNotification.MATCH, avecToky));

        // d'autres membres discutent aussi entre eux
        Correspondance autre = matcher(membres.get("Hanitra"), membres.get("Rado"));
        conversation(autre, maintenant.minusDays(1), List.of(
            new Object[] {membres.get("Rado"), "Bonjour Hanitra, comment vont les fraises du balcon ?"},
            new Object[] {membres.get("Hanitra"), "Trois fraises cette semaine, un record !"}
        ), null);

        // signalement ouvert pour l'espace de modération
        signalements.save(new Signalement(membres.get("Ony"), membres.get("Rojo"), null, MotifSignalement.FAUX_PROFIL,
            "Il demande du crédit téléphonique dès le premier message."));
        LOG.info("Démonstration prête : {} membres fictifs", MEMBRES.size() + 1);
    }

    private Utilisateur creer(Fiche f, Map<String, Ville> parNom, String motDePasse, int graine) {
        Ville ville = parNom.get(f.ville());
        if (ville == null) {
            throw new IllegalStateException("Ville de démonstration absente du référentiel : " + f.ville());
        }
        Utilisateur u = new Utilisateur();
        u.setEmail(f.prenom().toLowerCase() + ".demo@lovematch.mg");
        u.setMotDePasse(motDePasse);
        u.setPrenom(f.prenom());
        u.setNom(f.nom());
        // anniversaire passé de quelques mois : l'âge affiché reste stable pendant la démonstration
        u.setDateNaissance(Horloge.aujourdhui().minusYears(f.age()).minusMonths(1 + graine % 9));
        u.setGenre(f.genre());
        u.setVille(ville);
        u.setBio(f.bio());
        u.setRecherche(f.recherche());
        u.setGenreRecherche(f.cherche());
        u.setAgeMin(f.ageMin());
        u.setAgeMax(f.ageMax());
        u.setRayonKm(f.rayonKm());
        u.setDemo(true);
        u.getInterets().addAll(interets.findByCodeIn(List.of(f.interets().split(" "))));
        for (String code : LANGUES.getOrDefault(f.prenom(), "MG FR").split(" ")) {
            u.getLangues().add(Langue.valueOf(code));
        }
        u.setDerniereActivite(Horloge.maintenant().minusMinutes(graine * 37L));
        utilisateurs.save(u);

        photos.save(new Photo(u, PhotoServiceImpl.jpeg(Illustration.portrait(f.genre(), graine), 0.82f), "image/jpeg", 0));
        String paysage = PAYSAGES.get(f.prenom());
        if (paysage != null) {
            photos.save(new Photo(u, lirePaysage(paysage), "image/jpeg", 1));
        }
        return u;
    }

    private static byte[] lirePaysage(String nom) {
        try (InputStream flux = new ClassPathResource("demo/paysages/" + nom + ".jpg").getInputStream()) {
            return flux.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Photo de démonstration absente : " + nom, e);
        }
    }

    private Correspondance matcher(Utilisateur un, Utilisateur autre) {
        reactions.save(new Reaction(un, autre, Decision.LIKE));
        reactions.save(new Reaction(autre, un, Decision.LIKE));
        return correspondances.save(new Correspondance(un, autre));
    }

    // chaque message est envoyé quelques minutes après le précédent ; seul le dernier reçu par « lecteur » reste non lu
    private void conversation(Correspondance c, LocalDateTime debut, List<Object[]> lignes, Utilisateur lecteur) {
        LocalDateTime quand = debut;
        for (int i = 0; i < lignes.size(); i++) {
            Utilisateur auteur = (Utilisateur) lignes.get(i)[0];
            Message m = new Message(c, auteur, (String) lignes.get(i)[1]);
            m.antidater(quand);
            boolean dernier = i == lignes.size() - 1;
            if (lecteur == null || !dernier || auteur == lecteur) {
                m.setLuLe(quand.plusMinutes(2));
            }
            messages.save(m);
            quand = quand.plusMinutes(6 + i * 3);
        }
    }
}
