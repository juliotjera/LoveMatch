import { Injectable, effect, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterStateSnapshot, TitleStrategy } from '@angular/router';
import { I18n } from './i18n';

// Titre de l'onglet traduit, et mis à jour quand on change de langue
@Injectable({ providedIn: 'root' })
export class TitresTraduits extends TitleStrategy {
  private readonly titre = inject(Title);
  private readonly i18n = inject(I18n);
  private cle = 'titre.accueil';

  constructor() {
    super();
    effect(() => {
      this.i18n.langue();
      this.appliquer();
    });
  }

  override updateTitle(etat: RouterStateSnapshot) {
    this.cle = this.buildTitle(etat) ?? 'titre.accueil';
    this.appliquer();
  }

  private appliquer() {
    const page = this.i18n.t(this.cle);
    this.titre.setTitle(this.cle === 'titre.accueil' ? page : `${page} | LoveMatch`);
  }
}
