import { Routes } from '@angular/router';
import { administrateur, membreConnecte, visiteur } from './core/gardes';

// Les pages sont chargées à la demande ; « title » est une clé de traduction (voir core/titres.ts).
// Toute nouvelle route doit aussi être ajoutée à ConfigurationApplication (backend)
// pour qu'un rechargement de la page renvoie l'interface.
export const routes: Routes = [
  { path: '', pathMatch: 'full', canActivate: [visiteur], loadComponent: () => import('./pages/accueil').then((m) => m.Accueil), title: 'titre.accueil' },
  { path: 'connexion', canActivate: [visiteur], loadComponent: () => import('./pages/connexion').then((m) => m.Connexion), title: 'titre.connexion' },
  { path: 'inscription', canActivate: [visiteur], loadComponent: () => import('./pages/inscription').then((m) => m.Inscription), title: 'titre.inscription' },
  { path: 'confidentialite', loadComponent: () => import('./pages/confidentialite').then((m) => m.Confidentialite), title: 'titre.confidentialite' },
  { path: 'admin', canActivate: [administrateur], loadComponent: () => import('./pages/admin').then((m) => m.Admin), title: 'titre.admin' },
  {
    path: '',
    canActivate: [membreConnecte],
    loadComponent: () => import('./ui/coquille').then((m) => m.Coquille),
    children: [
      { path: 'decouvrir', loadComponent: () => import('./pages/decouvrir').then((m) => m.Decouvrir), title: 'titre.decouvrir' },
      { path: 'matchs', loadComponent: () => import('./pages/matchs').then((m) => m.Matchs), title: 'titre.matchs' },
      { path: 'matchs/:id', loadComponent: () => import('./pages/conversation').then((m) => m.Conversation), title: 'titre.conversation' },
      { path: 'notifications', loadComponent: () => import('./pages/notifications').then((m) => m.Notifications), title: 'titre.notifications' },
      { path: 'profil', loadComponent: () => import('./pages/profil').then((m) => m.Profil), title: 'titre.profil' },
    ],
  },
  { path: '**', loadComponent: () => import('./pages/introuvable').then((m) => m.Introuvable), title: 'titre.introuvable' },
];
