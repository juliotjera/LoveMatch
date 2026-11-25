import { provideHttpClient, withFetch, withInterceptors, withXsrfConfiguration } from '@angular/common/http';
import { ApplicationConfig, inject, isDevMode, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideServiceWorker } from '@angular/service-worker';
import { TitleStrategy, provideRouter, withComponentInputBinding, withInMemoryScrolling } from '@angular/router';
import { routes } from './app.routes';
import { I18n } from './core/i18n';
import { intercepteurErreurs, intercepteurLangue } from './core/intercepteur';
import { TitresTraduits } from './core/titres';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // le dictionnaire de la langue choisie est chargé avant le premier affichage
    provideAppInitializer(() => inject(I18n).demarrer()),
    provideRouter(routes, withComponentInputBinding(),
      withInMemoryScrolling({ scrollPositionRestoration: 'top', anchorScrolling: 'enabled' })),
    // le serveur dépose le jeton CSRF dans le cookie XSRF-TOKEN ; Angular le renvoie dans l'en-tête X-XSRF-TOKEN
    provideHttpClient(withFetch(), withInterceptors([intercepteurLangue, intercepteurErreurs]),
      withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' })),
    { provide: TitleStrategy, useClass: TitresTraduits },
    // application installable : l'interface est gardée sur le téléphone, seules les données passent par le réseau
    provideServiceWorker('ngsw-worker.js', { enabled: !isDevMode(), registrationStrategy: 'registerWhenStable:30000' }),
  ],
};
