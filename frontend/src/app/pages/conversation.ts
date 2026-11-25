import {
  ChangeDetectionStrategy, Component, DestroyRef, ElementRef, Injector, OnInit, afterNextRender, computed, inject, input,
  signal, viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../core/api';
import { Avertisseur } from '../core/avis';
import { I18n } from '../core/i18n';
import { messageErreur } from '../core/intercepteur';
import { Correspondance, Message } from '../core/modeles';
import { Session } from '../core/session';
import { TempsReel } from '../core/temps-reel';
import { Icone } from '../ui/icone';
import { Photo } from '../ui/photo';
import { formaterDate, jourDe } from '../ui/quand';
import { FenetreSignalement } from '../ui/signalement';

interface Groupe {
  jour: string;
  messages: Message[];
}

@Component({
  selector: 'lm-conversation',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, RouterLink, Icone, Photo, FenetreSignalement],
  template: `
    @if (correspondance(); as c) {
      <section class="conversation" [attr.aria-label]="t('conv.avec', { prenom: c.autre.prenom })">
        <header class="tete">
          <a routerLink="/matchs" class="btn btn-discret retour" [attr.aria-label]="t('conv.retour')"><lm-icone nom="retour" /></a>
          <button type="button" class="qui" (click)="fiche.showModal()">
            <lm-photo class="avatar" [id]="c.autre.photos[0]" [prenom]="c.autre.prenom" />
            <span>
              <span class="prenom">{{ c.autre.prenom }}</span>
              <span class="detail">{{ t('conv.voirProfil') }}</span>
            </span>
          </button>
          <details class="menu">
            <summary class="btn btn-discret" [attr.aria-label]="t('conv.options')">•••</summary>
            <div class="choix">
              @if (c.ouverte) {
                <button type="button" (click)="fermer()">{{ t('conv.fermer') }}</button>
              }
              <button type="button" (click)="bloquer()">{{ t('conv.bloquer', { prenom: c.autre.prenom }) }}</button>
              <button type="button" class="danger" (click)="signalement()?.ouvrir()">{{ t('signaler.lien') }}</button>
            </div>
          </details>
        </header>

        <div class="fil" #fil role="log" aria-live="polite" [attr.aria-label]="t('conv.messages')">
          @if (plusAnciens()) {
            <button type="button" class="btn btn-contour anciens" (click)="chargerAnciens()">{{ t('conv.anciens') }}</button>
          }
          @if (messages().length === 0) {
            <div class="debut">
              <p>{{ t('conv.debut', { prenom: c.autre.prenom }) }}</p>
              @if (communs().length) {
                <p class="idee">{{ t('conv.idee', { interet: communs()[0] }) }}</p>
              }
            </div>
          }
          @for (g of groupes(); track g.jour) {
            <p class="jour"><span>{{ g.jour }}</span></p>
            @for (m of g.messages; track m.id) {
              <div class="bulle" [class.moi]="m.auteurId === moiId()">
                <p dir="auto">{{ m.contenu }}</p>
                <span class="heure">{{ heure(m.envoyeLe) }}</span>
              </div>
            }
          }
          @if (dernierLu()) {
            <p class="lu">{{ t('conv.vu') }}</p>
          }
        </div>

        @if (c.ouverte) {
          <form class="saisie-message" (submit)="$event.preventDefault(); envoyer()">
            <label for="message" class="visuellement-cache">{{ t('conv.ecrire', { prenom: c.autre.prenom }) }}</label>
            <textarea id="message" name="message" rows="1" dir="auto" maxlength="1000" [(ngModel)]="texte"
              [placeholder]="t('conv.ecrire', { prenom: c.autre.prenom })" (keydown.enter)="entree($event)"
              (input)="ajuster($event)" #zone></textarea>
            @if (texte.length > 900) {
              <span class="reste" aria-live="polite">{{ 1000 - texte.length }}</span>
            }
            <button type="submit" class="btn btn-aimer envoyer" [disabled]="!texte.trim() || envoi()"
              [attr.aria-label]="t('conv.envoyer')">
              <lm-icone nom="envoyer" />
            </button>
          </form>
        } @else {
          <p class="fermee">{{ t('conv.fermeeTexte') }}</p>
        }
      </section>

      <dialog #fiche class="fiche" aria-labelledby="fiche-nom">
        <div class="photos">
          @for (p of c.autre.photos; track p) {
            <lm-photo [id]="p" [prenom]="c.autre.prenom" [alt]="t('decouvrir.photoDe', { prenom: c.autre.prenom, n: $index + 1, total: c.autre.photos.length })" />
          } @empty {
            <lm-photo [prenom]="c.autre.prenom" />
          }
        </div>
        <h2 id="fiche-nom">{{ c.autre.prenom }}, {{ c.autre.age }}</h2>
        <p class="detail">{{ i18n.joindre(c.autre.ville, c.autre.region, i18n.nomPays(c.autre.pays)) }}</p>
        <p class="cherche">{{ t('recherche.' + c.autre.recherche) }}</p>
        @if (c.autre.bio) {
          <p dir="auto">{{ c.autre.bio }}</p>
        }
        <ul class="pastilles">
          @for (i of c.autre.interets; track i.id) {
            <li class="pastille">{{ libelle(i) }}</li>
          }
        </ul>
        <button type="button" class="btn btn-contour" (click)="fiche.close()">{{ t('action.fermer') }}</button>
      </dialog>

      <lm-signalement [cibleId]="c.autre.id" [prenom]="c.autre.prenom" [message]="dernierRecu()"
        (traite)="apresSignalement($event.bloque)" />
    } @else if (introuvable()) {
      <div class="page">
        <div class="vide">
          <p>{{ t('conv.introuvable') }}</p>
          <a routerLink="/matchs" class="btn btn-principal">{{ t('matchs.titre') }}</a>
        </div>
      </div>
    }
  `,
  styles: `
    :host {
      display: block;
    }
    .conversation {
      display: grid;
      grid-template-rows: auto 1fr auto;
      height: calc(100dvh - 61px - var(--barre-basse));
      max-width: 820px;
      margin: 0 auto;
    }
    .tete {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 10px;
      border-bottom: 1px solid var(--trait);
      background: var(--carte);
    }
    .retour {
      padding: 0 8px;
    }
    .qui {
      display: flex;
      align-items: center;
      gap: 10px;
      flex: 1;
      min-width: 0;
      padding: 4px 8px;
      border: 0;
      border-radius: 12px;
      background: none;
      text-align: start;
      cursor: pointer;
    }
    .qui:hover {
      background: var(--jacaranda-pale);
    }
    .avatar {
      width: 44px;
      height: 44px;
      flex: none;
      --rayon: 50%;
      --format: 1;
      --taille-initiale: 1.1rem;
    }
    .qui > span {
      display: grid;
    }
    .prenom {
      font-weight: 700;
      font-size: 1.05rem;
    }
    .detail {
      color: var(--encre-douce);
      font-size: 0.88rem;
    }
    .menu {
      position: relative;
    }
    .menu summary {
      list-style: none;
      letter-spacing: 2px;
    }
    .menu summary::-webkit-details-marker {
      display: none;
    }
    .choix {
      position: absolute;
      inset-inline-end: 0;
      top: 52px;
      z-index: 10;
      display: grid;
      min-width: 240px;
      padding: 6px;
      background: var(--carte);
      border-radius: 14px;
      box-shadow: 0 16px 40px -12px rgb(31 24 51 / 35%);
    }
    .choix button {
      min-height: 44px;
      padding: 0 12px;
      border: 0;
      border-radius: 10px;
      background: none;
      text-align: start;
      cursor: pointer;
    }
    .choix button:hover {
      background: var(--jacaranda-pale);
    }
    .choix .danger {
      color: var(--erreur);
    }
    .fil {
      overflow-y: auto;
      padding: 16px 14px;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .anciens {
      align-self: center;
      min-height: 40px;
      margin-bottom: 8px;
    }
    .debut {
      margin: auto;
      display: grid;
      gap: 8px;
      text-align: center;
      max-width: 34ch;
      color: var(--encre-douce);
    }
    .idee {
      color: var(--flamboyant-texte);
      font-weight: 700;
    }
    .jour {
      align-self: center;
      margin: 12px 0 6px;
      padding: 2px 12px;
      border-radius: 999px;
      background: var(--jacaranda-pale);
      color: var(--jacaranda-fonce);
      font-size: 0.82rem;
      font-weight: 700;
    }
    .bulle {
      align-self: flex-start;
      max-width: min(78%, 520px);
      padding: 9px 13px 6px;
      border-radius: 18px;
      border-end-start-radius: 6px;
      background: var(--carte);
      box-shadow: 0 1px 2px rgb(31 24 51 / 8%);
    }
    .bulle p {
      white-space: pre-wrap;
      overflow-wrap: anywhere;
    }
    .bulle.moi {
      align-self: flex-end;
      border-radius: 18px;
      border-end-end-radius: 6px;
      background: var(--principal);
      color: var(--sur-couleur);
    }
    .heure {
      display: block;
      margin-top: 2px;
      font-size: 0.75rem;
      text-align: end;
      color: var(--encre-douce);
    }
    .moi .heure {
      color: #f1ecfa;
    }
    .lu {
      align-self: flex-end;
      color: var(--encre-douce);
      font-size: 0.8rem;
      font-weight: 700;
    }
    .saisie-message {
      position: relative;
      display: flex;
      align-items: flex-end;
      gap: 8px;
      padding: 10px 12px;
      border-top: 1px solid var(--trait);
      background: var(--carte);
    }
    textarea {
      flex: 1;
      min-height: 48px;
      max-height: 160px;
      padding: 12px 16px;
      border: 1.5px solid var(--trait-fort);
      border-radius: 24px;
      resize: none;
      background: var(--papier);
      line-height: 1.4;
    }
    textarea:focus-visible {
      outline-offset: 0;
      border-color: var(--jacaranda);
    }
    .reste {
      position: absolute;
      inset-inline-end: 76px;
      bottom: 22px;
      font-size: 0.8rem;
      color: var(--encre-douce);
    }
    .envoyer {
      width: 48px;
      padding: 0;
      flex: none;
    }
    .fermee {
      padding: 16px;
      text-align: center;
      color: var(--encre-douce);
      border-top: 1px solid var(--trait);
      background: var(--carte);
    }
    .fiche {
      width: min(440px, calc(100% - 24px));
      max-height: calc(100dvh - 24px);
      padding: 16px 16px 20px;
      border: 0;
      border-radius: 24px;
    }
    .fiche[open] {
      display: grid;
      gap: 12px;
    }
    .fiche::backdrop {
      background: rgb(31 24 51 / 60%);
    }
    .photos {
      display: grid;
      grid-auto-flow: column;
      grid-auto-columns: 82%;
      gap: 8px;
      overflow-x: auto;
      scroll-snap-type: x mandatory;
    }
    .photos lm-photo {
      scroll-snap-align: start;
    }
    .cherche {
      justify-self: start;
      padding: 3px 12px;
      border-radius: 999px;
      background: var(--ocre-pale);
      color: var(--ocre-texte);
      font-weight: 700;
      font-size: 0.9rem;
    }
    @media (min-width: 900px) {
      .conversation {
        height: calc(100dvh - 61px);
        border-inline: 1px solid var(--trait);
      }
    }
  `,
})
export class Conversation implements OnInit {
  private readonly api = inject(Api);
  private readonly avis = inject(Avertisseur);
  private readonly router = inject(Router);
  private readonly session = inject(Session);
  private readonly tempsReel = inject(TempsReel);
  private readonly destruction = inject(DestroyRef);
  private readonly injecteur = inject(Injector);
  protected readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;
  protected readonly libelle = this.i18n.interet;

  readonly id = input.required<string>();

  protected readonly correspondance = signal<Correspondance | null>(null);
  protected readonly messages = signal<Message[]>([]);
  protected readonly plusAnciens = signal(false);
  protected readonly introuvable = signal(false);
  protected readonly envoi = signal(false);
  protected texte = '';

  protected readonly moiId = computed(() => this.session.moi()?.id);
  private readonly fil = viewChild<ElementRef<HTMLElement>>('fil');
  private readonly zone = viewChild<ElementRef<HTMLTextAreaElement>>('zone');
  protected readonly signalement = viewChild(FenetreSignalement);

  protected readonly groupes = computed<Groupe[]>(() => {
    const locale = this.i18n.locale();
    const groupes: Groupe[] = [];
    for (const m of this.messages()) {
      const jour = jourDe(m.envoyeLe, locale, this.t);
      const dernier = groupes.at(-1);
      if (dernier?.jour === jour) {
        dernier.messages.push(m);
      } else {
        groupes.push({ jour, messages: [m] });
      }
    }
    return groupes;
  });

  // « Vu » sous mon dernier message s'il a été lu
  protected readonly dernierLu = computed(() => {
    const dernier = this.messages().at(-1);
    return dernier && dernier.auteurId === this.moiId() && dernier.luLe ? dernier : null;
  });

  protected readonly dernierRecu = computed(() => {
    const m = [...this.messages()].reverse().find((x) => x.auteurId !== this.moiId());
    return m ? { id: m.id, contenu: m.contenu } : null;
  });

  protected readonly communs = computed(() => {
    const c = this.correspondance();
    const moi = this.session.moi();
    if (!c || !moi) {
      return [];
    }
    const miens = new Set(moi.interets.map((i) => i.code));
    return c.autre.interets.filter((i) => miens.has(i.code)).map((i) => this.libelle(i).toLowerCase());
  });

  constructor() {
    this.tempsReel.evenements.pipe(takeUntilDestroyed(this.destruction)).subscribe((e) => {
      const c = this.correspondance();
      if (!c) {
        return;
      }
      if (e.type === 'MESSAGE' && e.donnees.correspondanceId === c.id) {
        this.ajouter(e.donnees);
        if (e.donnees.auteurId !== this.moiId()) {
          // conversation ouverte : le message est lu tout de suite
          this.api.messages(c.id).then(() => this.tempsReel.compter(), () => undefined);
        }
      } else if (e.type === 'LU' && e.donnees === c.id) {
        const maintenant = new Date().toISOString();
        this.messages.update((l) => l.map((m) => (m.auteurId === this.moiId() && !m.luLe ? { ...m, luLe: maintenant } : m)));
      } else if (e.type === 'MATCH_FERME' && e.donnees === c.id) {
        this.correspondance.set({ ...c, ouverte: false });
      }
    });
  }

  async ngOnInit() {
    const id = Number(this.id());
    try {
      const [c, messages] = await Promise.all([this.api.match(id), this.api.messages(id)]);
      this.correspondance.set(c);
      this.messages.set(messages);
      this.plusAnciens.set(messages.length === 30);
      this.tempsReel.compter();
      this.descendreApresAffichage();
    } catch {
      this.introuvable.set(true);
    }
  }

  heure(iso: string) {
    return formaterDate(iso, this.i18n.locale(), this.t, true);
  }

  async chargerAnciens() {
    const c = this.correspondance();
    const premier = this.messages()[0];
    if (!c || !premier) {
      return;
    }
    const anciens = await this.api.messages(c.id, premier.id);
    this.messages.update((l) => [...anciens, ...l]);
    this.plusAnciens.set(anciens.length === 30);
  }

  entree(evenement: Event) {
    const clavier = evenement as KeyboardEvent;
    if (!clavier.shiftKey) {
      clavier.preventDefault();
      this.envoyer();
    }
  }

  ajuster(evenement: Event) {
    const zone = evenement.target as HTMLTextAreaElement;
    zone.style.height = 'auto';
    zone.style.height = `${Math.min(zone.scrollHeight, 160)}px`;
  }

  async envoyer() {
    const c = this.correspondance();
    const contenu = this.texte.trim();
    if (!c || !contenu || this.envoi()) {
      return;
    }
    this.envoi.set(true);
    try {
      this.ajouter(await this.api.envoyer(c.id, contenu));
      this.texte = '';
      const zone = this.zone()?.nativeElement;
      if (zone) {
        zone.style.height = 'auto';
        zone.focus();
      }
    } catch (e) {
      this.avis.erreur(messageErreur(e, this.t('erreur.serveur')));
    } finally {
      this.envoi.set(false);
    }
  }

  async fermer() {
    const c = this.correspondance();
    if (!c || !confirm(this.t('conv.fermerConfirmer', { prenom: c.autre.prenom }))) {
      return;
    }
    await this.api.fermerMatch(c.id);
    this.avis.info(this.t('conv.fermeAvis'));
    await this.router.navigateByUrl('/matchs');
  }

  async bloquer() {
    const c = this.correspondance();
    if (!c || !confirm(this.t('conv.bloquerConfirmer', { prenom: c.autre.prenom }))) {
      return;
    }
    await this.api.bloquer(c.autre.id);
    this.avis.info(this.t('conv.bloqueAvis', { prenom: c.autre.prenom }));
    await this.router.navigateByUrl('/matchs');
  }

  apresSignalement(bloque: boolean) {
    if (bloque) {
      this.router.navigateByUrl('/matchs');
    }
  }

  private ajouter(m: Message) {
    if (this.messages().some((x) => x.id === m.id)) {
      return;
    }
    this.messages.update((l) => [...l, m]);
    this.descendreApresAffichage();
  }

  // le fil doit d'abord être affiché pour connaître sa hauteur
  private descendreApresAffichage() {
    afterNextRender(() => this.descendre(), { injector: this.injecteur });
  }

  private descendre() {
    const fil = this.fil()?.nativeElement;
    if (fil) {
      fil.scrollTop = fil.scrollHeight;
    }
  }
}
