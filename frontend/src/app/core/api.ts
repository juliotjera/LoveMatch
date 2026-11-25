import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  Correspondance, Decision, Interet, Message, Moi, MotifSignalement, NotificationLm, Page, ProfilPublic,
  ReponseReaction, Signalement, StatutSignalement, Statistiques, Suggestion, Ville, GenreRecherche, Genre, Recherche,
} from './modeles';

export interface Inscription {
  email: string;
  motDePasse: string;
  prenom: string;
  nom: string;
  dateNaissance: string;
  genre: Genre;
  villeId: number;
  genreRecherche: GenreRecherche;
  langues?: string[];
}

export interface EditionProfil {
  prenom: string;
  nom: string;
  villeId: number;
  bio: string;
  recherche: Recherche;
  interets: number[];
  langues: string[];
}

export interface Preferences {
  genreRecherche: GenreRecherche;
  ageMin: number;
  ageMax: number;
  rayonKm: number;
}

// Appels à l'API : chaque méthode renvoie une promesse (les composants utilisent async/await et des signaux)
@Injectable({ providedIn: 'root' })
export class Api {
  private readonly http = inject(HttpClient);

  private lire<T>(url: string, params?: HttpParams) {
    return firstValueFrom(this.http.get<T>(url, { params }));
  }

  private ecrire<T>(methode: 'POST' | 'PUT' | 'DELETE', url: string, corps?: unknown) {
    return firstValueFrom(this.http.request<T>(methode, url, { body: corps }));
  }

  // compte
  demoActive = () => this.lire<{ active: boolean }>('/api/auth/demo');
  essayer = (role: 'membre' | 'admin') => this.ecrire<Moi>('POST', `/api/auth/demo/${role}`);
  inscrire = (d: Inscription) => this.ecrire<Moi>('POST', '/api/auth/inscription', d);
  connecter = (email: string, motDePasse: string) => this.ecrire<Moi>('POST', '/api/auth/connexion', { email, motDePasse });
  deconnecter = () => this.ecrire<void>('POST', '/api/auth/deconnexion');

  // profil
  moi = () => this.lire<Moi>('/api/profil');
  modifierProfil = (d: EditionProfil) => this.ecrire<Moi>('PUT', '/api/profil', d);
  modifierPreferences = (d: Preferences) => this.ecrire<Moi>('PUT', '/api/profil/preferences', d);
  supprimerCompte = () => this.ecrire<void>('DELETE', '/api/profil');
  ajouterPhoto(fichier: File) {
    const formulaire = new FormData();
    formulaire.append('fichier', fichier);
    return firstValueFrom(this.http.post<{ id: number }>('/api/profil/photos', formulaire));
  }
  supprimerPhoto = (id: number) => this.ecrire<void>('DELETE', `/api/profil/photos/${id}`);
  photoPrincipale = (id: number) => this.ecrire<void>('PUT', `/api/profil/photos/${id}/principale`);
  membre = (id: number) => this.lire<ProfilPublic>(`/api/membres/${id}`);

  // découverte
  suggestions(page: number, distanceMax: number | null, interetsCommuns: boolean) {
    let params = new HttpParams().set('page', page).set('taille', 12).set('interetsCommuns', interetsCommuns);
    if (distanceMax) {
      params = params.set('distanceMax', distanceMax);
    }
    return this.lire<Page<Suggestion>>('/api/suggestions', params);
  }
  reagir = (cibleId: number, decision: Decision) => this.ecrire<ReponseReaction>('POST', '/api/reactions', { cibleId, decision });
  annulerPasse = (cibleId: number) => this.ecrire<void>('DELETE', `/api/reactions/passes/${cibleId}`);
  likesRecus = () => this.lire<{ nombre: number }>('/api/reactions/recues');

  // matchs et messages
  matchs = () => this.lire<Correspondance[]>('/api/matchs');
  match = (id: number) => this.lire<Correspondance>(`/api/matchs/${id}`);
  fermerMatch = (id: number) => this.ecrire<void>('DELETE', `/api/matchs/${id}`);
  messages(id: number, avant?: number) {
    let params = new HttpParams().set('taille', 30);
    if (avant) {
      params = params.set('avant', avant);
    }
    return this.lire<Message[]>(`/api/matchs/${id}/messages`, params);
  }
  envoyer = (id: number, contenu: string) => this.ecrire<Message>('POST', `/api/matchs/${id}/messages`, { contenu });

  // notifications
  notifications = () => this.lire<NotificationLm[]>('/api/notifications');
  nonLues = () => this.lire<{ nombre: number }>('/api/notifications/non-lues');
  toutLire = () => this.ecrire<void>('POST', '/api/notifications/lues');

  // sécurité
  bloques = () => this.lire<ProfilPublic[]>('/api/blocages');
  bloquer = (id: number) => this.ecrire<void>('POST', `/api/blocages/${id}`);
  debloquer = (id: number) => this.ecrire<void>('DELETE', `/api/blocages/${id}`);
  signaler = (cibleId: number, motif: MotifSignalement, commentaire: string, messageId?: number) =>
    this.ecrire<void>('POST', '/api/signalements', { cibleId, motif, commentaire, messageId });

  // modération
  statistiques = () => this.lire<Statistiques>('/api/admin/statistiques');
  signalements = () => this.lire<Signalement[]>('/api/admin/signalements');
  decider = (id: number, decision: StatutSignalement) =>
    this.ecrire<Signalement>('POST', `/api/admin/signalements/${id}/decision`, { decision });
  reactiver = (id: number) => this.ecrire<void>('POST', `/api/admin/membres/${id}/reactivation`);

  // référentiels
  // villes du monde dont le nom commence par « debut »
  villes = (debut: string) => this.lire<Ville[]>('/api/referentiels/villes', new HttpParams().set('q', debut).set('limite', 8));
  interets = () => this.lire<Interet[]>('/api/referentiels/interets');
}

export function photoUrl(id: number) {
  return `/api/photos/${id}`;
}
