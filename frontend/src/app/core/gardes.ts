import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Session } from './session';

// routes réservées aux membres connectés
export const membreConnecte: CanActivateFn = async (_, etat) => {
  const session = inject(Session);
  const router = inject(Router);
  const moi = await session.charger();
  if (!moi) {
    return router.createUrlTree(['/connexion'], { queryParams: { retour: etat.url } });
  }
  return moi.role === 'ADMIN' ? router.createUrlTree(['/admin']) : true;
};

export const administrateur: CanActivateFn = async () => {
  const session = inject(Session);
  const router = inject(Router);
  const moi = await session.charger();
  if (!moi) {
    return router.createUrlTree(['/connexion']);
  }
  return moi.role === 'ADMIN' ? true : router.createUrlTree(['/decouvrir']);
};

// pages d'accueil, de connexion et d'inscription : un membre déjà connecté va directement à son espace
export const visiteur: CanActivateFn = async () => {
  const session = inject(Session);
  const router = inject(Router);
  const moi = await session.charger();
  return moi ? router.createUrlTree([session.accueil(moi)]) : true;
};
