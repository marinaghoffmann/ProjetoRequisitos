import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, merge, timer } from 'rxjs';

import { NaoLidasService } from './core/api/nao-lidas.service';
import { AutenticacaoService } from './core/auth/autenticacao.service';

/** Intervalo da contagem de mensagens não lidas do menu (F-19 RN 11). */
const CONTAR_NAO_LIDAS_A_CADA_MS = 30000;

interface ItemMenu {
  rotulo: string;
  /** Rota do Angular. Sem rota = item ainda não implementado (aparece desabilitado). */
  rota?: string;
  /** Path SVG (viewBox 24x24). */
  icone: string;
  contador?: number;
  exato?: boolean;
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly autenticacao = inject(AutenticacaoService);
  protected readonly menuConta = signal(false);
  private readonly naoLidas = inject(NaoLidasService);

  /** "Mensagens" leva às conversas (F-19) e mostra o total real de não lidas (RN 11). */
  protected readonly principal = computed<ItemMenu[]>(() => [
    { rotulo: 'Início', rota: '/inicio', icone: 'M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z' },
    { rotulo: 'Descobrir', icone: 'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20ZM16.24 7.76l-2.12 6.36-6.36 2.12 2.12-6.36z' },
    {
      rotulo: 'Jogos',
      rota: '/catalogo',
      exato: true,
      icone: 'M6 11h4M8 9v4M15 12h.01M18 10h.01M17.3 5H6.7a4 4 0 0 0-3.9 3.2l-1.1 5.4A3 3 0 0 0 4.6 17L8 14.5h8l3.4 2.5a3 3 0 0 0 2.9-3.4l-1.1-5.4A4 4 0 0 0 17.3 5Z',
    },
    { rotulo: 'Lançamentos', rota: '/lancamentos', icone: 'M8 2v4M16 2v4M3 10h18M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2zM8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01' },
    { rotulo: 'Listas', rota: '/biblioteca/listas', icone: 'M3 6h.01M3 12h.01M3 18h.01M8 6h13M8 12h13M8 18h13' },
    { rotulo: 'Journal', rota: '/journal', icone: 'M12 7v14M3 18a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h5a4 4 0 0 1 4 4 4 4 0 0 1 4-4h5a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1h-6a3 3 0 0 0-3 3 3 3 0 0 0-3-3z' },
    {
      rotulo: 'Comunidade',
      rota: '/forum',
      exato: false,
      contador: this.naoLidas.total(),
      icone: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M16 3.128a4 4 0 0 1 0 7.744M22 21v-2a4 4 0 0 0-3-3.87M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z',
    },
    ...(this.autenticacao.temPapel('MODERADOR', 'ADMINISTRADOR') ? [{ rotulo: 'Moderação', rota: '/moderacao', icone: 'M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1zM9 12l2 2 4-4' }] : []),
    { rotulo: 'Marketplace', rota: '/marketplace', exato: true, icone: 'M16 10a4 4 0 0 1-8 0M3.103 6.034h17.794M3.4 5.467a2 2 0 0 0-.4 1.2V20a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6.667a2 2 0 0 0-.4-1.2l-2-2.667A2 2 0 0 0 17 2H7a2 2 0 0 0-1.6.8z' },
    { rotulo: 'Notificações', rota: '/notificacoes', icone: 'M10.268 21a2 2 0 0 0 3.464 0M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326' },
    {
      rotulo: 'Mensagens',
      rota: '/marketplace/mensagens',
      exato: false,
      contador: this.naoLidas.total(),
      icone: 'M22 17a2 2 0 0 1-2 2H6.828a2 2 0 0 0-1.414.586l-2.202 2.202A.71.71 0 0 1 2 21.286V5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2z',
    },
    { rotulo: 'Estatísticas', icone: 'M3 3v16a2 2 0 0 0 2 2h16M18 17V9M13 17V5M8 17v-3' },
  ]);

  constructor() {
    const navegacoes = inject(Router).events.pipe(filter((evento) => evento instanceof NavigationEnd));
    merge(timer(0, CONTAR_NAO_LIDAS_A_CADA_MS), navegacoes)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.naoLidas.atualizar());
  }

  protected readonly biblioteca: (ItemMenu & { ponto?: boolean })[] = [
    { rotulo: 'Em andamento', rota: '/biblioteca/jogando-agora', contador: 3, icone: '' },
    { rotulo: 'Wishlist', contador: 5, icone: 'M2 9.5a5.5 5.5 0 0 1 9.591-3.676.56.56 0 0 0 .818 0A5.49 5.49 0 0 1 22 9.5c0 2.29-1.5 4-3 5.5l-5.492 5.313a2 2 0 0 1-3 .019L5 15c-1.5-1.5-3-3.2-3-5.5' },
    { rotulo: 'Zerados', rota: '/biblioteca/zerados', icone: 'M10 14.66v1.626a2 2 0 0 1-.976 1.696A5 5 0 0 0 7 21.978M14 14.66v1.626a2 2 0 0 0 .976 1.696A5 5 0 0 1 17 21.978M18 9h1.5a1 1 0 0 0 0-5H18M4 22h16M6 9a6 6 0 0 0 12 0V3a1 1 0 0 0-1-1H7a1 1 0 0 0-1 1zM6 9H4.5a1 1 0 0 1 0-5H6' },
    { rotulo: 'Timeline', rota: '/timeline', icone: 'M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8M3 3v5h5M12 7v5l4 2' },
  ];

  /** Mesma regra do catálogo: só aceita /uploads/ ou http(s). */
  protected imagemUrl(caminho?: string | null): string {
    if (!caminho) return '';
    if (caminho.startsWith('/uploads/')) return caminho;
    if (caminho.startsWith('http://') || caminho.startsWith('https://')) return caminho;
    return '';
  }
}