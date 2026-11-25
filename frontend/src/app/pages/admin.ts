import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { Api } from '../core/api';
import { Avertisseur } from '../core/avis';
import { I18n } from '../core/i18n';
import { messageErreur } from '../core/intercepteur';
import { Signalement, StatutSignalement, Statistiques } from '../core/modeles';
import { Session } from '../core/session';
import { BoutonLangue } from '../ui/bouton-langue';
import { Logo } from '../ui/logo';
import { Quand } from '../ui/quand';

// Espace de modération : chiffres du jour et signalements à traiter
@Component({
  selector: 'lm-admin',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Logo, BoutonLangue, Quand],
  template: `
    <header class="barre">
      <div class="marque"><lm-logo /><span class="role">{{ t('admin.role') }}</span></div>
      <div class="outils">
        @if (session.moi()?.demo) {
          <span class="demo">{{ t('nav.demo') }}</span>
        }
        <lm-bouton-langue />
        <button type="button" class="btn btn-discret" (click)="session.fermer()">{{ t('nav.deconnexion') }}</button>
      </div>
    </header>

    <main id="contenu" class="page">
      <header class="entete-page">
        <h1>{{ t('admin.titre') }}</h1>
        <p>{{ t('admin.sous') }}</p>
      </header>

      @if (stats(); as s) {
        <dl class="chiffres">
          <div><dt>{{ t('admin.membres') }}</dt><dd>{{ s.membres }}</dd></div>
          <div><dt>{{ t('admin.matchs') }}</dt><dd>{{ s.matchsOuverts }}</dd></div>
          <div><dt>{{ t('admin.messages') }}</dt><dd>{{ s.messages24h }}</dd></div>
          <div [class.alerte-chiffre]="s.signalementsOuverts > 0"><dt>{{ t('admin.ouverts') }}</dt><dd>{{ s.signalementsOuverts }}</dd></div>
          <div><dt>{{ t('admin.suspendus') }}</dt><dd>{{ s.suspendus }}</dd></div>
        </dl>
      }

      <h2 class="titre-liste">{{ t('admin.aTraiter') }}</h2>
      @if (liste().length === 0) {
        <div class="vide"><p>{{ t('admin.rien') }}</p></div>
      } @else {
        <ul class="signalements">
          @for (s of liste(); track s.id) {
            <li class="panneau">
              <div class="tete">
                <span class="motif">{{ t('motif.' + s.motif) }}</span>
                <lm-quand class="quand" [date]="s.creeLe" />
              </div>
              <dl class="details">
                <div><dt>{{ t('admin.cible') }}</dt><dd>{{ s.cibleNomComplet }} <span class="email">{{ s.cibleEmail }}</span></dd></div>
                <div><dt>{{ t('admin.auteur') }}</dt><dd>{{ s.auteurPrenom }}</dd></div>
                <div><dt>{{ t('admin.contre') }}</dt><dd>{{ s.signalementsContreCible }}</dd></div>
              </dl>
              @if (s.commentaire) {
                <blockquote dir="auto">{{ s.commentaire }}</blockquote>
              }
              @if (s.messageSignale) {
                <p class="message"><strong>{{ t('admin.messageSignale') }}</strong> <bdi>{{ s.messageSignale }}</bdi></p>
              }
              <div class="decisions">
                <button type="button" class="btn btn-contour" (click)="decider(s, 'CLASSE')">{{ t('admin.classer') }}</button>
                <button type="button" class="btn btn-contour" (click)="decider(s, 'AVERTI')">{{ t('admin.avertir') }}</button>
                @if (s.cibleSuspendue) {
                  <button type="button" class="btn btn-contour" (click)="reactiver(s)">{{ t('admin.reactiver') }}</button>
                } @else {
                  <button type="button" class="btn btn-danger" (click)="decider(s, 'SUSPENDU')">{{ t('admin.suspendre') }}</button>
                }
              </div>
            </li>
          }
        </ul>
      }
    </main>
  `,
  styles: `
    .barre {
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 10px;
      padding: calc(10px + var(--haut-sur)) 16px 10px;
      background: var(--carte);
      border-bottom: 1px solid var(--trait);
    }
    .marque,
    .outils {
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .role {
      padding: 2px 10px;
      border-radius: 4px;
      background: var(--principal);
      color: var(--sur-couleur);
      font-size: 0.85rem;
      font-weight: 700;
    }
    .demo {
      padding: 4px 10px;
      border-radius: 4px;
      background: var(--ocre-pale);
      color: var(--ocre-texte);
      font-size: 0.85rem;
      font-weight: 700;
    }
    .page {
      padding-bottom: 40px;
    }
    .chiffres {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 14px 0;
      margin: 0 0 28px;
    }
    .chiffres div {
      padding: 4px 16px;
      border-inline-start: 1px solid var(--trait);
    }
    .chiffres .alerte-chiffre dd {
      color: var(--flamboyant-texte);
    }
    dt {
      color: var(--encre-douce);
      font-size: 0.9rem;
      font-weight: 700;
    }
    .chiffres dd {
      margin: 2px 0 0;
      font-family: var(--police-titre);
      font-size: 2rem;
      font-weight: 700;
      font-variant-numeric: tabular-nums;
    }
    .titre-liste {
      margin-bottom: 14px;
    }
    .signalements {
      display: grid;
      gap: 14px;
      margin: 0;
      padding: 0;
      list-style: none;
    }
    .signalements li {
      display: grid;
      gap: 12px;
    }
    .tete {
      display: flex;
      justify-content: space-between;
      gap: 10px;
    }
    .motif {
      font-weight: 700;
      color: var(--erreur);
    }
    .quand {
      color: var(--encre-douce);
      font-size: 0.9rem;
    }
    .details {
      display: grid;
      gap: 8px;
      margin: 0;
    }
    .details dd {
      margin: 0;
    }
    .email {
      color: var(--encre-douce);
      overflow-wrap: anywhere;
    }
    blockquote {
      margin: 0;
      padding: 8px 14px;
      border-inline-start: 4px solid var(--trait-fort);
      background: var(--papier);
      border-radius: 0;
      border-start-end-radius: 10px;
      border-end-end-radius: 10px;
    }
    .decisions {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
    @media (min-width: 640px) {
      .details {
        grid-template-columns: 2fr 1fr 1fr;
      }
    }
  `,
})
export class Admin implements OnInit {
  private readonly api = inject(Api);
  private readonly avis = inject(Avertisseur);
  protected readonly session = inject(Session);
  protected readonly t = inject(I18n).t;

  protected readonly stats = signal<Statistiques | null>(null);
  protected readonly liste = signal<Signalement[]>([]);

  async ngOnInit() {
    await this.charger();
  }

  private async charger() {
    const [stats, liste] = await Promise.all([this.api.statistiques(), this.api.signalements()]);
    this.stats.set(stats);
    this.liste.set(liste);
  }

  async decider(s: Signalement, decision: StatutSignalement) {
    if (decision === 'SUSPENDU' && !confirm(this.t('admin.suspendreConfirmer', { nom: s.cibleNomComplet }))) {
      return;
    }
    try {
      await this.api.decider(s.id, decision);
      this.avis.info(this.t('admin.decision.' + decision));
      await this.charger();
    } catch (e) {
      this.avis.erreur(messageErreur(e, this.t('erreur.serveur')));
    }
  }

  async reactiver(s: Signalement) {
    await this.api.reactiver(s.cibleId);
    this.avis.info(this.t('admin.reactive'));
    await this.charger();
  }
}
