import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { Avertisseur } from './avis';
import { I18n } from './i18n';
import { ErreurApi } from './modeles';
import { Session } from './session';

// l'API répond (messages d'erreur) dans la langue de l'interface
export const intercepteurLangue: HttpInterceptorFn = (requete, suite) => {
  const langue = inject(I18n).langue();
  return suite(requete.url.startsWith('/api/') ? requete.clone({ setHeaders: { 'Accept-Language': langue } }) : requete);
};

// Erreurs communes à tous les appels : session expirée, réseau coupé, trop de demandes
export const intercepteurErreurs: HttpInterceptorFn = (requete, suite) => {
  const session = inject(Session);
  const router = inject(Router);
  const avis = inject(Avertisseur);
  const i18n = inject(I18n);

  return suite(requete).pipe(
    catchError((erreur: HttpErrorResponse) => {
      const verificationDeSession = requete.method === 'GET' && requete.url === '/api/profil';
      if (erreur.status === 401 && !verificationDeSession && !requete.url.startsWith('/api/auth/')) {
        const etaitConnecte = session.connecte();
        session.perdue();
        if (etaitConnecte) {
          avis.erreur(i18n.t('erreur.session'));
        }
        router.navigate(['/connexion'], { queryParams: { retour: router.url } });
      } else if (erreur.status === 0) {
        avis.erreur(i18n.t('erreur.reseau'));
      } else if (erreur.status === 429 || erreur.status >= 500) {
        avis.erreur((erreur.error as ErreurApi | null)?.message ?? i18n.t('erreur.serveur'));
      }
      return throwError(() => erreur);
    }),
  );
};

// message lisible d'une erreur d'API, pour l'afficher à côté du formulaire concerné
export function messageErreur(erreur: unknown, parDefaut: string): string {
  if (erreur instanceof HttpErrorResponse) {
    const corps = erreur.error as ErreurApi | null;
    if (corps?.champs) {
      return Object.values(corps.champs).join(' ');
    }
    if (corps?.message) {
      return corps.message;
    }
  }
  return parDefaut;
}
