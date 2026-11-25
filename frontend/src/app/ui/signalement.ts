import { ChangeDetectionStrategy, Component, ElementRef, inject, input, output, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api } from '../core/api';
import { Avertisseur } from '../core/avis';
import { I18n } from '../core/i18n';
import { messageErreur } from '../core/intercepteur';
import { MotifSignalement } from '../core/modeles';

// Fenêtre « Signaler » : motif, précision facultative, et option de bloquer en même temps
@Component({
  selector: 'lm-signalement',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule],
  template: `
    <dialog #fenetre aria-labelledby="titre-signalement" (close)="ferme.emit()">
      <form method="dialog" (submit)="$event.preventDefault(); envoyer()">
        <h2 id="titre-signalement">{{ t('signaler.titre', { prenom: prenom() }) }}</h2>
        <p class="aide">{{ t('signaler.aide') }}</p>
        <fieldset class="champ">
          <legend>{{ t('signaler.motif') }}</legend>
          <div class="motifs">
            @for (m of motifs; track m) {
              <label class="motif">
                <input type="radio" name="motif" [value]="m" [(ngModel)]="motif" />
                <span>{{ t('motif.' + m) }}</span>
              </label>
            }
          </div>
        </fieldset>
        <div class="champ">
          <label for="precision">{{ t('signaler.precision') }}</label>
          <textarea id="precision" class="saisie" name="precision" maxlength="500" [(ngModel)]="commentaire"></textarea>
        </div>
        @if (message(); as m) {
          <label class="bloquer joindre">
            <input type="checkbox" name="joindre" [(ngModel)]="joindre" />
            <span>{{ t('signaler.joindre') }} <q dir="auto">{{ m.contenu.length > 90 ? m.contenu.slice(0, 90) + '…' : m.contenu }}</q></span>
          </label>
        }
        <label class="bloquer">
          <input type="checkbox" name="bloquer" [(ngModel)]="bloquer" />
          <span>{{ t('signaler.bloquerAussi', { prenom: prenom() }) }}</span>
        </label>
        <div class="boutons">
          <button type="button" class="btn btn-contour" (click)="fermer()">{{ t('action.annuler') }}</button>
          <button type="submit" class="btn btn-principal" [disabled]="!motif || occupe()">{{ t('signaler.envoyer') }}</button>
        </div>
      </form>
    </dialog>
  `,
  styles: `
    dialog {
      width: min(480px, calc(100% - 24px));
      max-height: calc(100dvh - 24px);
      padding: 24px 20px;
      border: 0;
      border-radius: 20px;
      box-shadow: 0 30px 80px -20px rgb(31 24 51 / 45%);
    }
    dialog::backdrop {
      background: rgb(31 24 51 / 55%);
    }
    form {
      display: grid;
      gap: 16px;
    }
    .aide {
      color: var(--encre-douce);
    }
    .motifs {
      display: grid;
      gap: 6px;
    }
    .motif,
    .bloquer {
      display: flex;
      gap: 10px;
      align-items: center;
      min-height: 44px;
      padding: 6px 12px;
      border: 1.5px solid var(--trait);
      border-radius: 10px;
      cursor: pointer;
    }
    .motif:has(input:checked) {
      border-color: var(--jacaranda-fonce);
      background: var(--jacaranda-pale);
    }
    .bloquer {
      border: 0;
      padding: 0;
    }
    .joindre {
      align-items: flex-start;
    }
    .joindre input {
      margin-top: 3px;
      flex: none;
    }
    q {
      color: var(--encre-douce);
    }
    input[type='radio'],
    input[type='checkbox'] {
      width: 20px;
      height: 20px;
      accent-color: var(--jacaranda-fonce);
    }
    .boutons {
      display: flex;
      justify-content: flex-end;
      flex-wrap: wrap;
      gap: 10px;
    }
  `,
})
export class FenetreSignalement {
  private readonly api = inject(Api);
  private readonly avis = inject(Avertisseur);
  protected readonly t = inject(I18n).t;

  readonly cibleId = input.required<number>();
  readonly prenom = input.required<string>();
  // dernier message reçu de cette personne, joint au signalement si on le souhaite
  readonly message = input<{ id: number; contenu: string } | null>(null);
  readonly ferme = output<void>();
  readonly traite = output<{ bloque: boolean }>();

  private readonly fenetre = viewChild.required<ElementRef<HTMLDialogElement>>('fenetre');
  protected readonly motifs: MotifSignalement[] = ['FAUX_PROFIL', 'HARCELEMENT', 'CONTENU_INAPPROPRIE', 'MINEUR', 'AUTRE'];
  protected motif: MotifSignalement | null = null;
  protected commentaire = '';
  protected bloquer = true;
  protected joindre = true;
  protected readonly occupe = signal(false);

  ouvrir() {
    this.motif = null;
    this.commentaire = '';
    this.bloquer = true;
    this.joindre = true;
    this.fenetre().nativeElement.showModal();
  }

  fermer() {
    this.fenetre().nativeElement.close();
  }

  async envoyer() {
    if (!this.motif) {
      return;
    }
    this.occupe.set(true);
    try {
      const joint = this.joindre ? this.message()?.id : undefined;
      await this.api.signaler(this.cibleId(), this.motif, this.commentaire.trim(), joint);
      if (this.bloquer) {
        await this.api.bloquer(this.cibleId());
      }
      this.avis.info(this.t('signaler.merci'));
      this.traite.emit({ bloque: this.bloquer });
      this.fermer();
    } catch (e) {
      this.avis.erreur(messageErreur(e, this.t('erreur.serveur')));
    } finally {
      this.occupe.set(false);
    }
  }
}
