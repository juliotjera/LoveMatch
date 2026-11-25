import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterOutlet } from '@angular/router';
import { SwUpdate } from '@angular/service-worker';
import { filter } from 'rxjs';
import { Avertisseur } from './core/avis';
import { I18n } from './core/i18n';

@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet],
  template: `
    <router-outlet />
    <div class="avis" aria-live="polite">
      @for (a of avis.liste(); track a.id) {
        <div class="un-avis" [class.erreur]="a.ton === 'erreur'" [attr.role]="a.ton === 'erreur' ? 'alert' : 'status'">
          <span>{{ a.texte }}</span>
          @if (a.action; as action) {
            <button type="button" (click)="action.faire(); avis.retirer(a.id)">{{ action.libelle }}</button>
          }
          <button type="button" class="fermer" (click)="avis.retirer(a.id)" [attr.aria-label]="t('action.fermer')">×</button>
        </div>
      }
    </div>
  `,
  styles: `
    /* téléphone : en haut de l'écran, pour ne cacher ni la carte ni les boutons J'aime et Passer */
    .avis {
      position: fixed;
      left: 50%;
      top: calc(70px + var(--haut-sur));
      z-index: 50;
      display: grid;
      gap: 8px;
      width: min(480px, calc(100% - 24px));
      transform: translateX(-50%);
      pointer-events: none;
    }
    .un-avis {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 8px 8px 8px 16px;
      border-radius: 10px;
      background: var(--avis-fond);
      color: #fff;
      box-shadow: var(--ombre-fenetre);
      pointer-events: auto;
      animation: apparaitre 0.2s ease-out;
    }
    .un-avis.erreur {
      background: var(--erreur);
    }
    .un-avis span {
      flex: 1;
    }
    button {
      min-height: 40px;
      padding: 0 12px;
      border: 0;
      border-radius: 10px;
      background: rgb(255 255 255 / 14%);
      color: #fff;
      font-weight: 700;
      cursor: pointer;
    }
    .fermer {
      width: 40px;
      padding: 0;
      background: none;
      font-size: 1.3rem;
    }
    @keyframes apparaitre {
      from { opacity: 0; transform: translateY(-6px); }
    }
    @media (min-width: 900px) {
      .avis {
        top: auto;
        bottom: 28px;
      }
    }
  `,
})
export class App {
  protected readonly avis = inject(Avertisseur);
  protected readonly t = inject(I18n).t;

  constructor() {
    // une nouvelle version a été téléchargée en arrière-plan : on propose de l'ouvrir
    const misesAJour = inject(SwUpdate);
    if (misesAJour.isEnabled) {
      misesAJour.versionUpdates
        .pipe(filter((e) => e.type === 'VERSION_READY'), takeUntilDestroyed())
        .subscribe(() => this.avis.info(this.t('maj.dispo'), { libelle: this.t('maj.recharger'), faire: () => location.reload() }));
    }
  }
}
