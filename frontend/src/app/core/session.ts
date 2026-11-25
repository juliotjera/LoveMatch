import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Api } from './api';
import { Moi } from './modeles';

// Le membre connecté. La session elle-même vit dans un cookie HttpOnly : le navigateur ne voit jamais le jeton.
@Injectable({ providedIn: 'root' })
export class Session {
  private readonly api = inject(Api);
  private readonly router = inject(Router);

  readonly moi = signal<Moi | null>(null);
  readonly connecte = computed(() => this.moi() !== null);
  readonly admin = computed(() => this.moi()?.role === 'ADMIN');
  private chargement: Promise<Moi | null> | null = null;

  // au premier passage : le cookie est-il encore valable ?
  charger(): Promise<Moi | null> {
    this.chargement ??= this.api.moi().then(
      (moi) => {
        this.moi.set(moi);
        return moi;
      },
      () => null,
    );
    return this.chargement;
  }

  ouvrir(moi: Moi) {
    this.moi.set(moi);
    this.chargement = Promise.resolve(moi);
  }

  async rafraichir() {
    this.ouvrir(await this.api.moi());
  }

  // la session a expiré côté serveur (401) : on revient à la connexion
  perdue() {
    this.moi.set(null);
    this.chargement = Promise.resolve(null);
  }

  async fermer() {
    try {
      await this.api.deconnecter();
    } finally {
      this.perdue();
      await this.router.navigateByUrl('/');
    }
  }

  accueil(moi: Moi) {
    if (moi.role === 'ADMIN') {
      return '/admin';
    }
    return moi.profilComplet ? '/decouvrir' : '/profil';
  }
}
