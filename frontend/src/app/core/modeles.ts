// Types renvoyés par l'API Spring Boot (dto/response)

export type Genre = 'HOMME' | 'FEMME';
export type GenreRecherche = 'HOMMES' | 'FEMMES' | 'TOUS';
export type Recherche = 'RELATION_SERIEUSE' | 'AMITIE' | 'A_DECOUVRIR';
export type Role = 'MEMBRE' | 'ADMIN';
export type Decision = 'LIKE' | 'PASSE';
export type MotifSignalement = 'FAUX_PROFIL' | 'HARCELEMENT' | 'CONTENU_INAPPROPRIE' | 'MINEUR' | 'AUTRE';
export type StatutSignalement = 'OUVERT' | 'CLASSE' | 'AVERTI' | 'SUSPENDU';
export type TypeNotification = 'LIKE' | 'MATCH' | 'MESSAGE';

export interface Ville {
  id: number;
  nom: string;
  region: string;
}

export interface Interet {
  id: number;
  code: string;
  libelleFr: string;
  libelleMg: string;
}

export interface Moi {
  id: number;
  email: string;
  prenom: string;
  nom: string;
  dateNaissance: string;
  age: number;
  genre: Genre;
  ville: Ville;
  bio: string | null;
  recherche: Recherche;
  interets: Interet[];
  langues: string[];
  photos: number[];
  genreRecherche: GenreRecherche;
  ageMin: number;
  ageMax: number;
  rayonKm: number;
  role: Role;
  demo: boolean;
  profilComplet: boolean;
}

export interface ProfilPublic {
  id: number;
  prenom: string;
  age: number;
  genre: Genre;
  ville: string;
  region: string;
  bio: string | null;
  recherche: Recherche;
  interets: Interet[];
  langues: string[];
  photos: number[];
}

export interface Score {
  total: number;
  age: number;
  distance: number;
  interets: number;
  langues: number;
  recherche: number;
  distanceKm: number;
  interetsCommuns: string[];
  languesCommunes: string[];
}

export interface Suggestion {
  profil: ProfilPublic;
  score: Score;
}

export interface Page<T> {
  elements: T[];
  page: number;
  taille: number;
  total: number;
}

export interface ReponseReaction {
  match: boolean;
  correspondanceId?: number;
}

export interface Message {
  id: number;
  correspondanceId: number;
  auteurId: number;
  contenu: string;
  envoyeLe: string;
  luLe?: string;
}

export interface Correspondance {
  id: number;
  autre: ProfilPublic;
  creeLe: string;
  ouverte: boolean;
  dernierMessage?: Message;
  nonLus: number;
}

export interface NotificationLm {
  id: number;
  type: TypeNotification;
  correspondanceId?: number;
  prenom?: string;
  lue: boolean;
  creeLe: string;
}

export interface Signalement {
  id: number;
  motif: MotifSignalement;
  commentaire?: string;
  statut: StatutSignalement;
  creeLe: string;
  auteurId: number;
  auteurPrenom: string;
  cibleId: number;
  cibleNomComplet: string;
  cibleEmail: string;
  cibleSuspendue: boolean;
  signalementsContreCible: number;
  messageSignale?: string;
}

export interface Statistiques {
  membres: number;
  suspendus: number;
  matchsOuverts: number;
  messages24h: number;
  signalementsOuverts: number;
}

export interface ErreurApi {
  statut: number;
  erreur: string;
  message: string;
  champs?: Record<string, string>;
}

// événements poussés par le serveur sur /user/queue/evenements
export type Evenement =
  | { type: 'MESSAGE'; donnees: Message }
  | { type: 'NOTIFICATION'; donnees: NotificationLm }
  | { type: 'LU'; donnees: number }
  | { type: 'MATCH_FERME'; donnees: number };
