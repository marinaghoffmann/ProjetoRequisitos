import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, merge, timer } from 'rxjs';

import { NaoLidasService } from './core/api/nao-lidas.service';
import { AutenticacaoService } from './core/auth/autenticacao.service';

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

  protected readonly principal: ItemMenu[] = [
    { rotulo: 'Início', rota: '/inicio', icone: 'M3 11 12 3l9 8M5 10v10h5v-6h4v6h5V10' },
    { rotulo: 'Descobrir', icone: 'M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18ZM15.5 8.5l-2 5-5 2 2-5z' },
    {
      rotulo: 'Jogos',
      rota: '/catalogo',
      exato: true,
      icone: 'M6 11h4M8 9v4M15 12h.01M18 10h.01M17.3 5H6.7a4 4 0 0 0-3.9 3.2l-1.1 5.4A3 3 0 0 0 4.6 17L8 14.5h8l3.4 2.5a3 3 0 0 0 2.9-3.4l-1.1-5.4A4 4 0 0 0 17.3 5Z',
    },
    { rotulo: 'Listas', icone: 'M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01' },
    { rotulo: 'Journal', icone: 'M5 3h14v18H5zM9 8h6M9 12h6' },
    {
      rotulo: 'Comunidade',
      rota: '/forum',
      exato: false,
      contador: 3,
      icone: 'M16 20v-1a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v1M9.5 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7ZM21 20v-1a4 4 0 0 0-3-3.9M16 4.1a3.5 3.5 0 0 1 0 6.8',
    },
    { rotulo: 'Marketplace', rota: '/marketplace', exato: true, icone: 'M5 8h14l-1 12H6zM9 8V6a3 3 0 0 1 6 0v2' },
    { rotulo: 'Mensagens', contador: 1, icone: 'M4 5h16v11H9l-5 4z' },
    { rotulo: 'Estatísticas', icone: 'M4 20V4M4 20h16M9 20v-7M14 20V9M19 20v-5' },
  ];

  protected readonly biblioteca: (ItemMenu & { ponto?: boolean })[] = [
    { rotulo: 'Em andamento', rota: '/biblioteca/jogando-agora', contador: 3, icone: '' },
    { rotulo: 'Wishlist', contador: 5, icone: 'M12 21s-8-5.2-8-11a4.6 4.6 0 0 1 8-3 4.6 4.6 0 0 1 8 3c0 5.8-8 11-8 11Z' },
    { rotulo: 'Zerados', contador: 5, icone: 'm5 12 5 5 9-10' },
    { rotulo: 'Timeline', icone: 'M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18ZM15.5 8.5l-2 5-5 2 2-5z' },
  ];

  /** Mesma regra do catálogo: só aceita /uploads/ ou http(s). */
  protected imagemUrl(caminho?: string | null): string {
    if (!caminho) return '';
    if (caminho.startsWith('/uploads/')) return caminho;
    if (caminho.startsWith('http://') || caminho.startsWith('https://')) return caminho;
    return '';
  }
}