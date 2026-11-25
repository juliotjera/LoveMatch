import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

// Icônes au trait (24 × 24), toujours décoratives : le texte voisin ou un aria-label porte le sens
const TRACES: Record<string, string> = {
  decouvrir: 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z M15.5 8.5l-2 5-5 2 2-5z',
  matchs: 'M4 5h11a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H9l-4 3v-3H4a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z M17 9h3a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-1v3l-4-3h-3',
  cloche: 'M6 16V11a6 6 0 1 1 12 0v5l2 2H4z M10 21h4',
  profil: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M4 21a8 8 0 0 1 16 0',
  bouclier: 'M12 3l8 3v6c0 4.5-3.4 8-8 9-4.6-1-8-4.5-8-9V6z M9 12l2 2 4-4',
  retour: 'M15 5l-7 7 7 7',
  envoyer: 'M4 12l16-8-6 16-2.5-6.5z M11.5 13.5L20 4',
  fermer: 'M6 6l12 12 M18 6L6 18',
  coeur: 'M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.6-7 10-7 10z',
  passer: 'M6 6l12 12 M18 6L6 18',
  annuler: 'M9 14L4 9l5-5 M4 9h10a6 6 0 0 1 0 12h-3',
  plus: 'M12 5v14 M5 12h14',
  drapeau: 'M5 21V4 M5 4h11l-2 4 2 4H5',
  lieu: 'M12 21s-6-5.6-6-11a6 6 0 1 1 12 0c0 5.4-6 11-6 11z M12 12.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z',
  reglages: 'M4 7h10 M18 7h2 M4 17h4 M12 17h8 M16 5v4 M10 15v4',
  sortir: 'M15 4h3a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-3 M10 16l-4-4 4-4 M6 12h10',
  langue: 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z M3 12h18 M12 3c2.5 2.6 3.8 5.6 3.8 9s-1.3 6.4-3.8 9c-2.5-2.6-3.8-5.6-3.8-9S9.5 5.6 12 3z',
  etoile: 'M12 4l2.4 5 5.6.6-4.2 3.8 1.2 5.6L12 16.2 7 19l1.2-5.6L4 9.6 9.6 9z',
  poubelle: 'M5 7h14 M10 7V4h4v3 M7 7l1 13h8l1-13',
};

@Component({
  selector: 'lm-icone',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg [attr.width]="taille()" [attr.height]="taille()" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"
      [class.miroir]="miroir()">
      <path [attr.d]="trace()" [attr.fill]="plein() ? 'currentColor' : 'none'" />
    </svg>
  `,
  styles: `
    :host { display: inline-flex; flex: none; }
    /* écriture de droite à gauche : les flèches changent de sens */
    :host-context([dir='rtl']) svg.miroir { transform: scaleX(-1); }
  `,
})
export class Icone {
  readonly nom = input.required<string>();
  readonly taille = input(22);
  readonly plein = input(false);
  protected readonly trace = computed(() => TRACES[this.nom()] ?? '');
  protected readonly miroir = computed(() => ['retour', 'envoyer', 'sortir', 'annuler'].includes(this.nom()));
}
