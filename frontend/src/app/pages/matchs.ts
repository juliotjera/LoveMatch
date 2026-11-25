import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { Api } from '../core/api';
import { I18n } from '../core/i18n';
import { Correspondance } from '../core/modeles';
import { Session } from '../core/session';
import { TempsReel } from '../core/temps-reel';
import { Photo } from '../ui/photo';
import { Quand } from '../ui/quand';

@Component({
  selector: 'lm-matchs',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, Photo, Quand],
  template: `
    <div class="page">
      <header class="entete-page">
        <h1>{{ t('matchs.titre') }}</h1>
        <p>{{ t('matchs.sous') }}</p>
      </header>

      @if (chargement()) {
        <p class="visuellement-cache" role="status">{{ t('chargement') }}</p>
      } @else if (liste().length === 0) {
        <div class="vide">
          <h2>{{ t('matchs.videTitre') }}</h2>
          <p>{{ t('matchs.vide') }}</p>
          <a routerLink="/decouvrir" class="btn btn-principal">{{ t('nav.decouvrir') }}</a>
        </div>
      } @else {
        <ul class="liste">
          @for (m of liste(); track m.id) {
            <li>
              <a [routerLink]="['/matchs', m.id]" class="ligne" [class.fermee]="!m.ouverte" [class.non-lue]="m.nonLus > 0">
                <lm-photo class="vignette" [id]="m.autre.photos[0]" [prenom]="m.autre.prenom" />
                <span class="corps">
                  <span class="haut">
                    <span class="prenom">{{ m.autre.prenom }}, {{ m.autre.age }}</span>
                    <lm-quand class="heure" [date]="m.dernierMessage?.envoyeLe ?? m.creeLe" />
                  </span>
                  <span class="apercu">
                    @if (!m.ouverte) {
                      {{ t('matchs.fermee') }}
                    } @else if (m.dernierMessage; as d) {
                      @if (d.auteurId === moiId()) {
                        <span class="vous">{{ t('matchs.vous') }}</span>
                      }
                      <bdi>{{ d.contenu }}</bdi>
                    } @else {
                      <span class="nouveau">{{ t('matchs.nouveau', { prenom: m.autre.prenom }) }}</span>
                    }
                  </span>
                </span>
                @if (m.nonLus > 0 && m.ouverte) {
                  <span class="compte">{{ m.nonLus }}<span class="visuellement-cache"> {{ t('matchs.nonLus') }}</span></span>
                }
              </a>
            </li>
          }
        </ul>
      }
    </div>
  `,
  styles: `
    .page {
      max-width: 760px;
    }
    .liste {
      display: grid;
      gap: 2px;
      margin: 0;
      padding: 0;
      list-style: none;
      background: var(--carte);
      border-radius: var(--rayon-panneau);
      box-shadow: var(--ombre-carte);
      overflow: hidden;
    }
    .ligne {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 12px 16px;
      color: inherit;
      text-decoration: none;
      border-bottom: 1px solid var(--trait);
    }
    li:last-child .ligne {
      border-bottom: 0;
    }
    .ligne:hover {
      background: #faf8fd;
    }
    .ligne:focus-visible {
      outline-offset: -3px;
    }
    .vignette {
      width: 58px;
      height: 58px;
      flex: none;
      --rayon: 50%;
      --format: 1;
      --taille-initiale: 1.4rem;
    }
    .corps {
      display: grid;
      min-width: 0;
      flex: 1;
      gap: 2px;
    }
    .haut {
      display: flex;
      justify-content: space-between;
      gap: 8px;
    }
    .prenom {
      font-weight: 700;
      font-size: 1.05rem;
    }
    .heure {
      color: var(--encre-douce);
      font-size: 0.85rem;
      flex: none;
    }
    .apercu {
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
      color: var(--encre-douce);
    }
    .non-lue .apercu {
      color: var(--encre);
      font-weight: 700;
    }
    .vous {
      color: var(--encre-douce);
      font-weight: 400;
    }
    .nouveau {
      color: #8a2c10;
      font-weight: 700;
    }
    .fermee {
      opacity: 0.6;
    }
    .compte {
      min-width: 24px;
      height: 24px;
      padding: 0 7px;
      border-radius: 999px;
      background: var(--flamboyant);
      color: #fff;
      font-size: 0.8rem;
      font-weight: 700;
      line-height: 24px;
      text-align: center;
    }
  `,
})
export class Matchs implements OnInit {
  private readonly api = inject(Api);
  private readonly tempsReel = inject(TempsReel);
  private readonly destruction = inject(DestroyRef);
  private readonly session = inject(Session);
  protected readonly t = inject(I18n).t;
  protected readonly moiId = computed(() => this.session.moi()?.id);

  protected readonly liste = signal<Correspondance[]>([]);
  protected readonly chargement = signal(true);

  constructor() {
    this.tempsReel.evenements.pipe(takeUntilDestroyed(this.destruction)).subscribe((e) => {
      if (e.type === 'MESSAGE' || e.type === 'MATCH_FERME' || (e.type === 'NOTIFICATION' && e.donnees.type === 'MATCH')) {
        this.charger();
      }
    });
  }

  async ngOnInit() {
    await this.charger();
  }

  private async charger() {
    try {
      this.liste.set(await this.api.matchs());
    } finally {
      this.chargement.set(false);
    }
  }
}
