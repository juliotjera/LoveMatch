import { Injectable, signal } from '@angular/core';

export interface Avis {
  id: number;
  texte: string;
  ton: 'info' | 'erreur';
  action?: { libelle: string; faire: () => void };
}

// Messages brefs en bas de l'écran (annoncés aux lecteurs d'écran par une région aria-live)
@Injectable({ providedIn: 'root' })
export class Avertisseur {
  readonly liste = signal<Avis[]>([]);
  private suivant = 1;

  info(texte: string, action?: Avis['action']) {
    this.ajouter({ id: this.suivant++, texte, ton: 'info', action });
  }

  erreur(texte: string) {
    this.ajouter({ id: this.suivant++, texte, ton: 'erreur' });
  }

  retirer(id: number) {
    this.liste.update((l) => l.filter((a) => a.id !== id));
  }

  private ajouter(avis: Avis) {
    this.liste.update((l) => [...l.slice(-2), avis]);
    setTimeout(() => this.retirer(avis.id), avis.action ? 7000 : 4500);
  }
}
