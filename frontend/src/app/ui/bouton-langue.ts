import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { I18n, LANGUES, Langue } from '../core/i18n';
import { Icone } from './icone';

// Choix de la langue : pastille compacte (globe + code) au-dessus d'une liste native, accessible et adaptée au téléphone
@Component({
  selector: 'lm-bouton-langue',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icone],
  template: `
    <span class="pastille-langue" aria-hidden="true">
      <lm-icone nom="langue" [taille]="18" />
      <span>{{ i18n.langue().toUpperCase() }}</span>
    </span>
    <select [value]="i18n.langue()" (change)="choisir($event)" [attr.aria-label]="i18n.t('langue.choisir')">
      @for (l of langues; track l.code) {
        <option [value]="l.code" [attr.lang]="l.code" [selected]="l.code === i18n.langue()">{{ l.nom }}</option>
      }
    </select>
  `,
  styles: `
    :host {
      position: relative;
      display: inline-flex;
    }
    .pastille-langue {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      min-height: 40px;
      padding: 0 12px;
      border: 1.5px solid var(--trait-fort);
      border-radius: 8px;
      background: var(--carte);
      color: var(--jacaranda-fonce);
      font-weight: 700;
      font-size: 0.85rem;
      letter-spacing: 0.02em;
    }
    select {
      position: absolute;
      inset: 0;
      width: 100%;
      opacity: 0;
      cursor: pointer;
      font-size: 16px;
    }
    :host:has(select:focus-visible) .pastille-langue {
      outline: 3px solid var(--jacaranda);
      outline-offset: 2px;
    }
    :host:hover .pastille-langue {
      border-color: var(--jacaranda-fonce);
    }
  `,
})
export class BoutonLangue {
  protected readonly i18n = inject(I18n);
  protected readonly langues = LANGUES;

  choisir(evenement: Event) {
    this.i18n.choisir((evenement.target as HTMLSelectElement).value as Langue);
  }
}
