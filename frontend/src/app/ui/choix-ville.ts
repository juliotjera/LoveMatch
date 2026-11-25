import { ChangeDetectionStrategy, Component, ElementRef, inject, input, model, signal, viewChild } from '@angular/core';
import { Api } from '../core/api';
import { I18n } from '../core/i18n';
import { Ville } from '../core/modeles';

let compteur = 0;

/**
 * Choix d'une ville parmi les villes du monde, au fil de la frappe (motif « combobox » des pratiques ARIA du W3C) :
 * flèches haut et bas pour parcourir, Entrée pour choisir, Échap pour fermer ; le nombre de résultats est annoncé.
 */
@Component({
  selector: 'lm-choix-ville',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="champ">
      <label [for]="idChamp">{{ libelle() }}</label>
      <div class="zone">
        <input #champ [id]="idChamp" class="saisie" type="text" role="combobox" autocomplete="off" spellcheck="false"
          aria-autocomplete="list" [attr.aria-expanded]="ouvert()" [attr.aria-controls]="idListe"
          [attr.aria-activedescendant]="actif() >= 0 ? idListe + '-' + actif() : null"
          [attr.aria-describedby]="idAide" [attr.aria-invalid]="invalide() ? true : null"
          [value]="texte()" (input)="saisir($event)" (keydown)="clavier($event)" (blur)="quitter()"
          (focus)="ouvrirSiResultats()" [placeholder]="i18n.t('ville.chercher')" />
        @if (ouvert()) {
          <ul class="liste" role="listbox" [id]="idListe" [attr.aria-label]="libelle()">
            @for (v of resultats(); track v.id; let i = $index) {
              <li role="option" [id]="idListe + '-' + i" [attr.aria-selected]="i === actif()" [class.actif]="i === actif()"
                (mousedown)="$event.preventDefault(); choisir(v)" (mouseenter)="actif.set(i)">
                <span class="nom">{{ v.nom }}</span>
                <span class="lieu">{{ lieu(v) }}</span>
              </li>
            }
          </ul>
        }
      </div>
      <p [id]="idAide" class="aide" [class.erreur-champ]="invalide()">{{ invalide() ? i18n.t('ville.choisir') : aide() }}</p>
      <p class="visuellement-cache" aria-live="polite">{{ annonce() }}</p>
    </div>
  `,
  styles: `
    .zone {
      position: relative;
    }
    .liste {
      position: absolute;
      inset-inline: 0;
      top: calc(100% + 4px);
      z-index: 40;
      max-height: 300px;
      overflow-y: auto;
      margin: 0;
      padding: 6px;
      list-style: none;
      background: var(--carte);
      border: 1.5px solid var(--trait);
      border-radius: 12px;
      box-shadow: 0 16px 40px -12px rgb(31 24 51 / 35%);
    }
    li {
      display: grid;
      gap: 1px;
      min-height: 44px;
      padding: 8px 12px;
      border-radius: 8px;
      cursor: pointer;
    }
    li.actif {
      background: var(--jacaranda-pale);
    }
    .nom {
      font-weight: 700;
    }
    .lieu {
      color: var(--encre-douce);
      font-size: 0.9rem;
    }
  `,
})
export class ChoixVille {
  private readonly api = inject(Api);
  protected readonly i18n = inject(I18n);

  readonly ville = model<Ville | null>(null);
  readonly libelle = input.required<string>();
  readonly aide = input('');
  readonly erreur = input(false);

  protected readonly idChamp = `ville-${++compteur}`;
  protected readonly idListe = `${this.idChamp}-liste`;
  protected readonly idAide = `${this.idChamp}-aide`;
  protected readonly resultats = signal<Ville[]>([]);
  protected readonly ouvert = signal(false);
  protected readonly actif = signal(-1);
  protected readonly annonce = signal('');
  protected readonly saisie = signal<string | null>(null);
  private readonly champ = viewChild.required<ElementRef<HTMLInputElement>>('champ');
  private minuterie: ReturnType<typeof setTimeout> | undefined;
  private derniere = 0;

  // tant qu'on n'a pas tapé, le champ affiche la ville choisie
  protected texte() {
    const saisie = this.saisie();
    if (saisie !== null) {
      return saisie;
    }
    const v = this.ville();
    return v ? this.i18n.joindre(v.nom, this.i18n.nomPays(v.pays)) : '';
  }

  protected invalide() {
    return this.erreur() && !this.ville();
  }

  protected lieu(v: Ville) {
    return this.i18n.joindre(v.region !== v.nom ? v.region : null, this.i18n.nomPays(v.pays));
  }

  protected saisir(evenement: Event) {
    const valeur = (evenement.target as HTMLInputElement).value;
    this.saisie.set(valeur);
    this.ville.set(null);
    clearTimeout(this.minuterie);
    this.minuterie = setTimeout(() => this.chercher(valeur), 200);
  }

  private async chercher(texte: string) {
    const demande = ++this.derniere;
    if (!texte.trim()) {
      this.resultats.set([]);
      this.ouvert.set(false);
      return;
    }
    try {
      const villes = await this.api.villes(texte.trim());
      if (demande !== this.derniere) {
        return; // une frappe plus récente a déjà répondu
      }
      this.resultats.set(villes);
      this.actif.set(villes.length ? 0 : -1);
      this.ouvert.set(villes.length > 0);
      this.annonce.set(villes.length ? this.i18n.t('ville.resultats', { n: villes.length }) : this.i18n.t('ville.aucune'));
    } catch {
      this.ouvert.set(false);
    }
  }

  protected clavier(evenement: KeyboardEvent) {
    const n = this.resultats().length;
    if (evenement.key === 'ArrowDown' && n) {
      evenement.preventDefault();
      this.ouvert.set(true);
      this.actif.update((i) => (i + 1) % n);
    } else if (evenement.key === 'ArrowUp' && n) {
      evenement.preventDefault();
      this.ouvert.set(true);
      this.actif.update((i) => (i - 1 + n) % n);
    } else if (evenement.key === 'Enter' && this.ouvert() && this.actif() >= 0) {
      evenement.preventDefault();
      this.choisir(this.resultats()[this.actif()]);
    } else if (evenement.key === 'Escape' && this.ouvert()) {
      evenement.preventDefault();
      this.ouvert.set(false);
    }
  }

  protected choisir(v: Ville) {
    this.ville.set(v);
    this.saisie.set(null);
    this.ouvert.set(false);
    this.annonce.set(this.i18n.joindre(v.nom, this.i18n.nomPays(v.pays)));
    this.champ().nativeElement.focus();
  }

  protected ouvrirSiResultats() {
    if (this.saisie() !== null && this.resultats().length) {
      this.ouvert.set(true);
    }
  }

  protected quitter() {
    this.ouvert.set(false);
  }
}
