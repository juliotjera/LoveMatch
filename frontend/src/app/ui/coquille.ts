import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { I18n } from '../core/i18n';
import { Session } from '../core/session';
import { TempsReel } from '../core/temps-reel';
import { Icone } from './icone';
import { Logo } from './logo';
import { BoutonLangue } from './bouton-langue';

// Cadre de l'espace membre : barre d'onglets en bas sur téléphone, colonne à gauche sur grand écran
@Component({
  selector: 'lm-coquille',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Icone, Logo, BoutonLangue],
  template: `
    <a class="lien-evitement" href="#contenu">{{ t('nav.evitement') }}</a>
    <header class="haut">
      <a routerLink="/decouvrir" class="marque" [attr.aria-label]="t('nav.accueil')"><lm-logo /></a>
      <div class="outils">
        @if (session.moi()?.demo) {
          <span class="demo">{{ t('nav.demo') }}</span>
        }
        <lm-bouton-langue />
      </div>
    </header>

    <nav class="onglets" [attr.aria-label]="t('nav.principale')">
      <ul>
        @for (o of onglets; track o.lien) {
          <li>
            <a [routerLink]="o.lien" routerLinkActive="actif" #lien="routerLinkActive"
              [attr.aria-current]="lien.isActive ? 'page' : null">
              <span class="pictogramme">
                <lm-icone [nom]="o.icone" />
                @if (o.badge && o.badge() > 0) {
                  <span class="badge">{{ o.badge() > 9 ? '9+' : o.badge() }}</span>
                }
              </span>
              <span class="texte">{{ t(o.texte) }}</span>
              @if (o.badge && o.badge() > 0) {
                <span class="visuellement-cache">{{ t('nav.nouveaux', { n: o.badge() }) }}</span>
              }
            </a>
          </li>
        }
      </ul>
      <button type="button" class="sortie" (click)="session.fermer()">
        <lm-icone nom="sortir" />
        <span class="texte">{{ t('nav.deconnexion') }}</span>
      </button>
    </nav>

    <main id="contenu" tabindex="-1">
      <router-outlet />
    </main>
  `,
  styles: `
    :host {
      display: block;
      min-height: 100dvh;
    }
    .haut {
      position: sticky;
      top: 0;
      z-index: 20;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
      padding: calc(10px + var(--haut-sur)) 12px 10px;
      background: var(--papier);
      border-bottom: 1px solid var(--trait);
    }
    .marque {
      text-decoration: none;
    }
    .outils {
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .demo {
      padding: 3px 8px;
      border-radius: 4px;
      background: var(--ocre-pale);
      color: var(--ocre-texte);
      font-size: 0.78rem;
      font-weight: 700;
    }
    .onglets {
      position: fixed;
      inset: auto 0 0 0;
      z-index: 30;
      height: var(--barre-basse);
      background: var(--carte);
      border-top: 1px solid var(--trait);
      padding-bottom: env(safe-area-inset-bottom);
    }
    .onglets ul {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      height: 100%;
      margin: 0;
      padding: 0;
      list-style: none;
    }
    .onglets a,
    .sortie {
      display: grid;
      place-items: center;
      align-content: center;
      gap: 2px;
      height: 100%;
      color: var(--encre-douce);
      text-decoration: none;
      font-size: 0.78rem;
      font-weight: 700;
    }
    .onglets li {
      position: relative;
    }
    /* onglet actif : trait en haut de l'onglet et couleur, comme une barre d'onglets native */
    .onglets a.actif {
      color: var(--jacaranda-fonce);
    }
    .onglets a.actif::before {
      content: '';
      position: absolute;
      top: 0;
      inset-inline: 22%;
      height: 3px;
      border-radius: 0 0 3px 3px;
      background: var(--jacaranda-fonce);
    }
    .pictogramme {
      position: relative;
      display: grid;
      place-items: center;
      width: 52px;
      height: 30px;
    }
    .badge {
      position: absolute;
      top: -4px;
      inset-inline-end: 2px;
      min-width: 20px;
      height: 20px;
      padding: 0 5px;
      border-radius: 999px;
      background: var(--flamboyant);
      color: var(--sur-couleur);
      font-size: 0.72rem;
      line-height: 20px;
      text-align: center;
    }
    .sortie {
      display: none;
      border: 0;
      background: none;
      cursor: pointer;
    }
    main:focus {
      outline: none;
    }
    @media (min-width: 900px) {
      :host {
        display: grid;
        grid-template-columns: 232px 1fr;
        grid-template-rows: auto 1fr;
      }
      .haut {
        grid-column: 1 / -1;
      }
      .onglets {
        position: sticky;
        top: calc(64px + var(--haut-sur));
        height: calc(100dvh - 64px - var(--haut-sur));
        border-top: 0;
        border-inline-end: 1px solid var(--trait);
        background: transparent;
        display: flex;
        flex-direction: column;
        padding: 16px 12px;
      }
      .onglets ul {
        display: flex;
        flex-direction: column;
        gap: 4px;
        height: auto;
      }
      .onglets a,
      .sortie {
        display: flex;
        justify-content: flex-start;
        gap: 10px;
        height: 48px;
        padding: 0 10px;
        border-radius: 8px;
        font-size: 1rem;
      }
      .onglets a:hover,
      .sortie:hover {
        background: var(--jacaranda-pale);
      }
      .onglets a.actif {
        background: var(--jacaranda-pale);
      }
      .onglets a.actif::before {
        display: none;
      }
      .pictogramme {
        width: 30px;
      }
      .sortie {
        margin-top: auto;
      }
      main {
        --barre-basse: 0px;
      }
    }
  `,
})
export class Coquille implements OnInit, OnDestroy {
  protected readonly session = inject(Session);
  private readonly tempsReel = inject(TempsReel);
  protected readonly t = inject(I18n).t;

  protected readonly onglets = [
    { lien: '/decouvrir', icone: 'decouvrir', texte: 'nav.decouvrir', badge: undefined },
    { lien: '/matchs', icone: 'matchs', texte: 'nav.matchs', badge: this.tempsReel.messagesNonLus },
    { lien: '/notifications', icone: 'cloche', texte: 'nav.notifications', badge: this.tempsReel.nonLues },
    { lien: '/profil', icone: 'profil', texte: 'nav.profil', badge: undefined },
  ];

  ngOnInit() {
    this.tempsReel.demarrer();
  }

  ngOnDestroy() {
    this.tempsReel.arreter();
  }
}
