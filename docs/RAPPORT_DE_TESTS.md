# Rapport de tests

Date : 25 novembre 2026. Version : 2.1 (cahier des charges version 2, ouverture internationale).

## 1. Tests automatisés du serveur

Exécutés deux fois, sur H2 (mode PostgreSQL) et sur un vrai PostgreSQL 18 ; la même suite tourne dans GitHub Actions sur PostgreSQL 17.

| Test | Critère | H2 | PostgreSQL |
| --- | --- | --- | --- |
| On ne voit jamais une personne que le genre, l'âge, la distance ou l'absence de langue commune excluent, dans les deux sens ; un like envoyé à la main à un profil exclu est refusé | 8.1 | réussi | réussi |
| Un profil déjà liké ou passé ne revient pas ; « Revoir » annule un passe | 4.4.2 | réussi | réussi |
| Un profil incomplet ne reçoit pas de suggestions | 4.2.4 | réussi | réussi |
| Pas de conversation sans match ; une personne extérieure ne peut ni lire ni écrire ; après un blocage, plus aucun message dans les deux sens | 8.2 | réussi | réussi |
| Le nom de famille et l'e-mail n'apparaissent dans aucune réponse destinée à un autre membre (suggestions, profil, matchs, messages, notifications) | 8.3 | réussi | réussi |
| Les erreurs de l'API sont rédigées dans la langue demandée (français, anglais, chinois, arabe, malagasy, espagnol) | 8.6 | réussi | réussi |
| Inscription réservée aux majeurs ; e-mail unique (sans tenir compte des majuscules) | 4.1.1 | réussi | réussi |
| Session obligatoire, jeton CSRF exigé, compte suspendu déconnecté, modération réservée à l'administrateur | 4.8 | réussi | réussi |
| Score identique dans les deux sens, entre 0 et 100, égal à la somme des cinq critères, sur toutes les paires du jeu de démonstration (32 membres) | 8.4 | réussi | réussi |
| Le score suit le bon sens ; un profil hors rayon ou sans langue commune n'est jamais compatible ; une langue commune vaut 10 points, trois en valent 15 | 4.3 | réussi | réussi |
| « Essayer sans compte » ouvre toujours les mêmes comptes, même après recréation de la démonstration | 4.12 | réussi | réussi |
| Villes du monde : recherche sans accents (« montré » → Montréal), la plus peuplée d'abord (Paris, France avant Paris, Texas), région et pays renvoyés | 4.10.1 | réussi | réussi |
| Antananarivo et Paris ne se voient que lorsque les deux rayons couvrent les 8 755 km | 8.9 | réussi | réussi |
| Genre non binaire proposé seulement à qui cherche « tout le monde », et réciproquement | 4.10.4 | réussi | réussi |
| Les dates partent avec leur fuseau horaire (+03:00) | 8.9 | réussi | réussi |

Total : 15 tests, 0 échec, sur chacune des deux bases.

## 2. Dictionnaires

`npm run traductions` vérifie que les sept langues (malagasy, français, anglais, espagnol, chinois, hindi, arabe) ont les mêmes 324 textes et les mêmes variables (`{prenom}`, `{n}`…). Réussi ; exécuté aussi dans GitHub Actions.

## 3. Parcours vérifiés dans le navigateur

Sur l'image de production (interface servie par Spring Boot, base PostgreSQL, en-têtes de sécurité actifs) :

- accueil, « Essayer sans compte », suggestions, J'aime avec match immédiat (« C'est réciproque »), raccourcis clavier ;
- notification reçue en temps réel par WebSocket ;
- conversation : envoi, historique, défilement au dernier message, « Vu » ;
- profil : photos, préférences, langues parlées (ajout de l'espagnol enregistré) ;
- changement de langue à chaud dans les sept langues ; arabe entièrement retourné (de droite à gauche), messages des membres gardant leur propre sens d'écriture ;
- inscription en anglais depuis le Japon : ville choisie au clavier (« tok », flèche bas, Entrée), bouton « Afficher » du mot de passe, compte créé avec l'anglais comme langue parlée ;
- rayon « Partout dans le monde » : la diaspora apparaît (« Paris, France, à 8 755 km ») ;
- espace de modération ;
- application installable : service worker actif, puis page rechargée **hors réseau** avec succès (Edge, réseau coupé par l'outil de développement).

## 4. Accessibilité (WCAG 2.2 AA)

Audit axe-core 4.14 (règles WCAG 2.0, 2.1 et 2.2, niveaux A et AA) sur les 11 pages (accueil, connexion, inscription, confidentialité, découvrir, matchs, conversation, notifications, profil, modération, page introuvable), en français, en arabe (droite à gauche), en hindi et en chinois. **0 violation.**

## 5. Téléphone (critère 8.5)

Toutes les pages ont été affichées à 375 px de large, en français et en arabe : aucun défilement horizontal ; boutons d'au moins 44 px de haut ; barre d'onglets en bas d'écran.

## 6. Performance

Premier chargement de l'interface : 83 Ko transférés (compression gzip), pages et dictionnaires chargés à la demande ; fichiers à empreinte gardés un an par le navigateur ; interface conservée par le service worker. Photos ramenées à 960 px par le serveur.
