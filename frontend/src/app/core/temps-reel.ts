import { Injectable, inject, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { Subject } from 'rxjs';
import { Api } from './api';
import { Evenement } from './modeles';

// WebSocket STOMP : le serveur pousse messages, notifications et accusés de lecture.
// Les envois passent par l'API REST ; ici on ne fait qu'écouter.
@Injectable({ providedIn: 'root' })
export class TempsReel {
  private readonly api = inject(Api);
  private client: Client | null = null;

  readonly evenements = new Subject<Evenement>();
  readonly enLigne = signal(false);
  readonly nonLues = signal(0);
  readonly messagesNonLus = signal(0);

  demarrer() {
    if (this.client) {
      return;
    }
    const protocole = location.protocol === 'https:' ? 'wss' : 'ws';
    this.client = new Client({
      brokerURL: `${protocole}://${location.host}/ws`,
      reconnectDelay: 4000,
      heartbeatIncoming: 20000,
      heartbeatOutgoing: 20000,
    });
    this.client.onConnect = () => {
      this.enLigne.set(true);
      this.client?.subscribe('/user/queue/evenements', (trame) => {
        const evenement = JSON.parse(trame.body) as Evenement;
        if (evenement.type === 'NOTIFICATION') {
          this.nonLues.update((n) => n + 1);
        }
        this.evenements.next(evenement);
      });
      // ce qui est arrivé pendant la coupure
      this.compter();
    };
    this.client.onWebSocketClose = () => this.enLigne.set(false);
    this.client.activate();
    this.compter();
  }

  arreter() {
    this.client?.deactivate();
    this.client = null;
    this.enLigne.set(false);
    this.nonLues.set(0);
    this.messagesNonLus.set(0);
  }

  async compter() {
    try {
      const [notifications, matchs] = await Promise.all([this.api.nonLues(), this.api.matchs()]);
      this.nonLues.set(notifications.nombre);
      this.messagesNonLus.set(matchs.reduce((total, m) => total + (m.ouverte ? m.nonLus : 0), 0));
    } catch {
      // hors ligne : on réessaiera à la reconnexion
    }
  }
}
