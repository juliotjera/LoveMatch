# Cahier des charges : LoveMatch (version 2)

LoveMatch est né d'un sujet d'examen JSP de Licence 2 (« site de rencontre avec matching intelligent »), réalisé seul. Cette version 2 le réécrit entièrement : nouveau produit, nouveau calcul, Spring Boot 4.1 et Angular 22, application installable. Les numéros permettent de citer chaque exigence.

## 1. Le produit

1.1. **Pour qui.** Né à Madagascar, ouvert au monde : les personnes majeures qui vivent à Madagascar (étudiants, jeunes actifs, communautés qui n'y parlent ni malgache ni français), la diaspora malgache, et toute personne ailleurs dans le monde.
1.2. **Le problème.** Les applications de rencontre courantes classent les profils par photo et par un algorithme opaque ; elles ignorent la barrière de la langue, et celles qui sont locales oublient la diaspora.
1.3. **La promesse.** Chaque suggestion montre un **score de compatibilité sur 100, expliqué critère par critère**, et ne propose que des personnes avec qui la discussion est possible : préférences réciproques, distance acceptée des deux côtés, au moins une langue commune.
1.4. **Ce qui ne se négocie pas.** Majeurs uniquement ; on ne peut écrire qu'après un like réciproque ; nom de famille, e-mail et position jamais montrés ; blocage et signalement accessibles partout ; suppression complète du compte en un geste.

## 2. Ce qui change par rapport au sujet d'origine

| Sujet d'origine (L2) | Version 2 |
| --- | --- |
| Profil : nom, prénom, âge, lieu, préférence de genre (homme, femme) | Prénom (nom privé), âge, genre (femme, homme, **non binaire**), ville du monde, bio, 3 à 8 centres d'intérêt, ce que l'on recherche, **langues parlées**, jusqu'à 4 photos |
| Score : âge, lieu, genre | **Score en cinq brins** : âge, distance réelle entre villes, intérêts communs, langues communes, intention ; détail affiché |
| Préférence de genre vérifiée d'un seul côté | **Filtres réciproques** : genre, tranche d'âge, rayon et langue doivent convenir aux deux |
| « Même ville » = points | Distance réelle entre **31 800 villes du monde** (GeoNames, formule de haversine), rayon choisi par chacun, de 10 km à « partout dans le monde » |
| Un like envoie une notification | Like anonyme ; **match** si réciproque ; **messagerie en temps réel** ; notifications en direct |
| Aucune sécurité | Mots de passe hachés, session en cookie HttpOnly, CSRF, limites de débit, blocage, signalement, modération |
| Une langue | **Sept langues** : malagasy, français, et les cinq langues les plus parlées au monde (anglais, chinois, hindi, espagnol, arabe), arabe écrit de droite à gauche |
| Site web | Site, **application installable** (PWA, fonctionne en réseau faible) et **application Android** |

## 3. Acteurs

3.1. **Visiteur** : découvre le produit, crée un compte, ou essaie un compte de démonstration (membre ou modération).
3.2. **Membre** : complète son profil, reçoit des suggestions, like ou passe, discute avec ses matchs, bloque ou signale.
3.3. **Modération** (administrateur) : traite les signalements, avertit, suspend ou réactive un compte, consulte les chiffres du service.

## 4. Fonctionnalités

### 4.1. Compte
4.1.1. Inscription : e-mail, mot de passe (8 caractères minimum, bouton « Afficher »), prénom, nom, date de naissance (18 ans minimum, contrôlé par le serveur), genre (femme, homme, non binaire), ville du monde choisie au fil de la frappe, genre(s) recherché(s) (hommes, femmes, tout le monde) ; la langue de l'interface devient la première langue parlée.
4.1.2. Connexion par e-mail et mot de passe ; jeton JWT dans un cookie HttpOnly, SameSite=Strict ; déconnexion.
4.1.3. Limitation des tentatives de connexion par adresse IP.
4.1.4. Suppression du compte par le membre, avec toutes ses données (photos, likes, matchs, messages).

### 4.2. Profil
4.2.1. Prénom, nom (jamais affiché aux autres), date de naissance (seul l'âge est affiché), genre, ville, bio (500 caractères), centres d'intérêt (3 à 8), ce que l'on recherche, langues parlées (1 à 5 parmi 12).
4.2.2. Préférences : genre(s) recherché(s), tranche d'âge, rayon de recherche (de 10 km à « partout dans le monde »).
4.2.3. Jusqu'à 4 photos, redimensionnées et réenregistrées par le serveur (les métadonnées cachées disparaissent) ; une photo principale. Sans photo, l'initiale du prénom s'affiche.
4.2.4. Un profil sans bio, ou avec moins de trois centres d'intérêt, n'apparaît pas dans les suggestions et n'en reçoit pas.

### 4.3. Score de compatibilité (sur 100)
4.3.1. **Filtres obligatoires et réciproques** : genre recherché par chacun, âge de chacun dans la tranche de l'autre, distance dans le rayon des deux, **au moins une langue commune**, comptes actifs, aucun blocage.
4.3.2. **Âge (25 points)** : plein score quand chacun est au centre de la tranche recherchée par l'autre, la moitié au bord.
4.3.3. **Distance (25 points)** : plein score sous 5 km, puis décroissance continue jusqu'au plus petit des deux rayons.
4.3.4. **Centres d'intérêt (25 points)** : intérêts communs divisés par intérêts réunis (indice de Jaccard).
4.3.5. **Langues (15 points)** : une langue commune 10, deux ou plus 15.
4.3.6. **Intention (10 points)** : même recherche 10 ; l'un des deux « à découvrir » 5 ; sinon 0.
4.3.7. Le score est symétrique (A→B = B→A) ; son détail est renvoyé par l'API et dessiné sur chaque suggestion.

### 4.4. Suggestions
4.4.1. Un profil à la fois, trié par score décroissant puis par activité récente.
4.4.2. Une personne déjà likée, passée ou bloquée n'est plus proposée ; « passer » peut être annulé.
4.4.3. Filtres rapides : distance maximale, centres d'intérêt communs.
4.4.4. Raccourcis clavier : flèche gauche pour passer, flèche droite pour aimer.

### 4.5. Likes et matchs
4.5.1. Liker ou passer ; 100 likes par jour au maximum.
4.5.2. Un like reste anonyme (« quelqu'un vous a liké ») ; s'il est réciproque, c'est un match et les deux sont prévenus.
4.5.3. Liste des matchs avec le dernier message et le nombre de messages non lus.
4.5.4. « Ne plus être en contact » ferme la conversation.

### 4.6. Messagerie
4.6.1. Conversation uniquement entre deux personnes qui ont matché.
4.6.2. Messages en temps réel (WebSocket STOMP), historique paginé, indicateur « Vu ».
4.6.3. 1 000 caractères par message ; 30 messages par minute au maximum.
4.6.4. Une idée de premier message tirée d'un centre d'intérêt commun.

### 4.7. Notifications
4.7.1. Nouveau like (anonyme), nouveau match, nouveau message ; en temps réel, dans la cloche et sur les onglets ; marquées lues à l'ouverture.

### 4.8. Sécurité et modération
4.8.1. Bloquer une personne : elle disparaît des suggestions, des matchs et de la messagerie, dans les deux sens.
4.8.2. Signaler un profil avec un motif (faux profil, harcèlement, contenu inapproprié, mineur, autre) ; depuis une conversation, le dernier message reçu peut être joint.
4.8.3. La modération classe, avertit, suspend ou réactive ; le nom complet et l'e-mail ne sont visibles que d'elle.
4.8.4. Un compte suspendu perd immédiatement l'accès, même avec une session encore valable.

### 4.9. Langues
4.9.1. Interface en malagasy, français, anglais, espagnol, chinois, hindi et arabe ; choix à tout moment, sans rechargement ; langue du téléphone détectée à la première visite.
4.9.2. L'arabe s'affiche de droite à gauche ; les textes écrits par les membres gardent leur propre sens d'écriture.
4.9.3. Les erreurs renvoyées par l'API sont rédigées dans la langue de l'interface (en-tête Accept-Language).
4.9.4. Chaque dictionnaire n'est téléchargé que lorsque sa langue est choisie.

### 4.10. International
4.10.1. 31 800 villes de plus de 15 000 habitants (GeoNames) ; recherche au fil de la frappe sans tenir compte des accents ; pays affiché dans la langue de l'interface.
4.10.2. Le pays d'une suggestion n'est précisé que s'il diffère du sien ; distances et nombres au format de la langue (« 8 755 km », « 8,755 km »).
4.10.3. Les dates sont envoyées avec leur fuseau horaire et affichées à l'heure de chaque membre.
4.10.4. « Tout le monde » inclut les personnes non binaires ; « hommes » et « femmes » ne les incluent pas.

### 4.11. Application installable et Android
4.11.1. PWA : installable depuis le navigateur, interface gardée sur l'appareil (ouverture hors réseau, mises à jour en arrière-plan, avis « nouvelle version prête »).
4.11.2. Application Android (APK signé) qui ouvre le site en ligne, avec retour arrière natif et envoi de photos depuis la galerie ou l'appareil photo.

### 4.12. Démonstration
4.12.1. 32 membres fictifs à Madagascar et dans la diaspora (Paris, La Réunion, Maurice, Montréal), de plusieurs langues et genres, portraits dessinés (jamais de visage réel), paysages réels de Madagascar sous licence libre, matchs et messages déjà en cours, un signalement à traiter.
4.12.2. Boutons « Essayer sans compte » (membre, modération), désactivables ; données recréées chaque nuit.

## 5. Règles de gestion

5.1. Âge minimum 18 ans, calculé à partir de la date de naissance.
5.2. Un like par paire (membre, profil) ; un match par paire de personnes.
5.3. Une conversation n'existe qu'entre deux personnes ayant matché, et se ferme si l'une bloque l'autre ou met fin au contact.
5.4. Le nom de famille et l'e-mail ne sont jamais renvoyés aux autres membres.
5.5. Les distances sont calculées entre les centres des villes, jamais à partir d'une position de l'utilisateur.
5.6. Deux personnes sans langue commune ne sont jamais proposées l'une à l'autre.

## 6. Architecture

6.1. **Serveur** Spring Boot 4.1, Java 25 : paquet `com.esmia.lovematch` avec `config`, `controller/rest`, `dto/request`, `dto/response`, `entity`, `enumeration`, `exception`, `mapper`, `repository`, `security`, `service` et `service/impl`, `util` (mêmes conventions que les projets de L3).
6.2. **API REST** documentée (OpenAPI), codes HTTP corrects, erreurs au format uniforme et traduites (MessageSource), validation des entrées.
6.3. **Temps réel** : WebSocket STOMP authentifié par le même cookie.
6.4. **Base** : PostgreSQL en production, migrations versionnées (Flyway) ; H2 en mode PostgreSQL pour le développement.
6.5. **Interface** Angular 22 : composants autonomes, signaux, sans zone.js, routes protégées et chargées à la demande, intercepteurs (langue, erreurs), dictionnaires JSON chargés à la demande, service worker.
6.6. **Déploiement** : une seule image Docker (le serveur sert l'interface compilée), Render + Neon ; application Android Capacitor.

## 7. Exigences graphiques et ergonomiques

7.1. Identité propre, inspirée de Madagascar (jacaranda et flamboyant, score « tissé » comme un lamba), sans copier une application existante.
7.2. Mobile d'abord ; accessibilité WCAG 2.2 AA (contrastes, clavier, lecteurs d'écran, cibles de 44 px).
7.3. Réseau faible : premier chargement léger, fichiers mis en cache, photos allégées par le serveur.

## 8. Critères d'acceptation

8.1. On ne voit jamais une personne dont le genre, l'âge, la distance ou l'absence de langue commune vous exclut, dans un sens comme dans l'autre (test automatisé).
8.2. Une conversation est impossible sans match, et se ferme après un blocage (test automatisé).
8.3. Le nom de famille et l'e-mail n'apparaissent dans aucune réponse destinée à un autre membre (test automatisé).
8.4. Le score est identique quel que soit le sens du calcul et vaut la somme de ses cinq critères (test automatisé).
8.5. Toutes les pages s'affichent sur un téléphone de 375 px de large.
8.6. Les sept dictionnaires ont les mêmes textes et les mêmes variables (vérification automatique) ; les erreurs de l'API sont traduites (test automatisé).
8.7. Aucune violation WCAG 2.2 AA (axe-core) sur les pages, y compris en arabe.
8.8. L'application déjà ouverte une fois s'affiche sans réseau.
8.9. Deux membres de pays différents ne se voient que si leurs deux rayons couvrent la distance ; les dates portent leur fuseau horaire (tests automatisés).
