import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { I18n } from '../core/i18n';
import { BoutonLangue } from './bouton-langue';
import { Logo } from './logo';

// Mise en page des écrans de connexion et d'inscription
@Component({
  selector: 'lm-cadre-visiteur',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, Logo, BoutonLangue],
  template: `
    <header class="barre">
      <a routerLink="/" [attr.aria-label]="t('nav.accueil')"><lm-logo /></a>
      <lm-bouton-langue />
    </header>
    <main id="contenu" class="carte">
      <h1>{{ titre() }}</h1>
      @if (sousTitre()) {
        <p class="sous">{{ sousTitre() }}</p>
      }
      <ng-content />
    </main>
  `,
  styles: `
    :host {
      display: block;
      min-height: 100dvh;
      padding-bottom: 40px;
      background: var(--papier);
    }
    .barre {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: min(1120px, 100%);
      margin: 0 auto;
      padding: calc(14px + var(--haut-sur)) 16px 14px;
    }
    .barre a {
      text-decoration: none;
    }
    .carte {
      width: min(540px, calc(100% - 32px));
      margin: 12px auto 0;
      padding: 28px 22px;
      background: var(--carte);
      border: 1px solid var(--trait);
      border-radius: 12px;
      display: grid;
      gap: 8px;
    }
    h1 {
      font-size: clamp(1.8rem, 5vw, 2.4rem);
    }
    .sous {
      color: var(--encre-douce);
      margin-bottom: 14px;
    }
    @media (min-width: 640px) {
      .carte {
        padding: 36px 36px;
      }
    }
  `,
})
export class CadreVisiteur {
  protected readonly t = inject(I18n).t;
  readonly titre = input.required<string>();
  readonly sousTitre = input('');
}
