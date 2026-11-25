import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { Api } from '../core/api';
import { I18n } from '../core/i18n';
import { NotificationLm } from '../core/modeles';
import { TempsReel } from '../core/temps-reel';
import { Icone } from '../ui/icone';
import { Quand } from '../ui/quand';

@Component({
  selector: 'lm-notifications',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, Icone, Quand],
  template: `
    <div class="page">
      <header class="entete-page">
        <h1>{{ t('notif.titre') }}</h1>
        <p>{{ t('notif.sous') }}</p>
      </header>

      @if (!chargement() && liste().length === 0) {
        <div class="vide">
          <p>{{ t('notif.vide') }}</p>
          <a routerLink="/decouvrir" class="btn btn-principal">{{ t('nav.decouvrir') }}</a>
        </div>
      } @else {
        <ul class="liste">
          @for (n of liste(); track n.id) {
            <li [class.non-lue]="!n.lue" [attr.data-type]="n.type">
              <span class="pastille-type"><lm-icone [nom]="icone(n)" [plein]="n.type === 'LIKE'" [taille]="20" /></span>
              <span class="texte">
                @switch (n.type) {
                  @case ('LIKE') { {{ t('notif.like') }} }
                  @case ('MATCH') { {{ t('notif.match', { prenom: n.prenom ?? '' }) }} }
                  @case ('MESSAGE') { {{ t('notif.message', { prenom: n.prenom ?? '' }) }} }
                }
                <lm-quand class="quand" [date]="n.creeLe" />
              </span>
              @if (n.type === 'LIKE') {
                <a routerLink="/decouvrir" class="btn btn-discret">{{ t('nav.decouvrir') }}</a>
              } @else if (n.correspondanceId) {
                <a [routerLink]="['/matchs', n.correspondanceId]" class="btn btn-discret">{{ t('notif.ouvrir') }}</a>
              }
              @if (!n.lue) {
                <span class="visuellement-cache">{{ t('notif.nouvelle') }}</span>
              }
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
      gap: 0;
      border-top: 1px solid var(--trait);
      margin: 0;
      padding: 0;
      list-style: none;
    }
    li {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 8px;
      border-bottom: 1px solid var(--trait);
    }
    /* non lue : texte en gras sur fond teinté, comme une boîte de réception */
    li.non-lue {
      background: var(--jacaranda-pale);
    }
    li.non-lue .texte {
      font-weight: 700;
    }
    .pastille-type {
      display: grid;
      place-items: center;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      flex: none;
      background: var(--jacaranda-pale);
      color: var(--jacaranda-fonce);
    }
    [data-type='LIKE'] .pastille-type,
    [data-type='MATCH'] .pastille-type {
      background: var(--flamboyant-pale);
      color: var(--flamboyant);
    }
    .texte {
      display: grid;
      flex: 1;
      min-width: 0;
    }
    .quand {
      color: var(--encre-douce);
      font-size: 0.85rem;
    }
  `,
})
export class Notifications implements OnInit {
  private readonly api = inject(Api);
  private readonly tempsReel = inject(TempsReel);
  private readonly destruction = inject(DestroyRef);
  protected readonly t = inject(I18n).t;

  protected readonly liste = signal<NotificationLm[]>([]);
  protected readonly chargement = signal(true);

  constructor() {
    this.tempsReel.evenements.pipe(takeUntilDestroyed(this.destruction)).subscribe((e) => {
      if (e.type === 'NOTIFICATION') {
        this.liste.update((l) => [e.donnees, ...l]);
        this.api.toutLire().then(() => this.tempsReel.nonLues.set(0), () => undefined);
      }
    });
  }

  async ngOnInit() {
    try {
      this.liste.set(await this.api.notifications());
      // la page est ouverte : tout est vu (l'affichage garde la marque « nouveau » jusqu'au prochain passage)
      await this.api.toutLire();
      this.tempsReel.nonLues.set(0);
    } finally {
      this.chargement.set(false);
    }
  }

  icone(n: NotificationLm) {
    return n.type === 'MESSAGE' ? 'matchs' : 'coeur';
  }
}
