import { Component, effect, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { EstatisticaComunidade, EstatisticasService, VisibilidadeEstatistica } from '../../core/api/estatisticas.service';
import { ErroApi } from '../../core/http/erro-api';
import { BadgeStatusComponent } from '../../shared/ui/badge-status.component';
import { CabecalhoPaginaComponent } from '../../shared/ui/cabecalho-pagina.component';
import { ChipsComponent, OpcaoChip } from '../../shared/ui/chips.component';
import { CarregandoComponent, ErroComponent, EstadoVazioComponent } from '../../shared/ui/estados.component';
import { IconeComponent } from '../../shared/ui/icone.component';

/**
 * F-17: as fichas de todos os jogos. Nas ocultas (menos de 3 avaliações, RN 3) a API só manda o total de
 * avaliações, então as outras colunas mostram "—". A ficha guarda contagens: sem percentual nem valor (P9).
 */
@Component({
  selector: 'app-estatisticas',
  imports: [DecimalPipe, RouterLink, BadgeStatusComponent, CabecalhoPaginaComponent, ChipsComponent,
    CarregandoComponent, ErroComponent, EstadoVazioComponent, IconeComponent],
  templateUrl: './estatisticas.page.html',
  styleUrl: './estatisticas.page.scss',
})
export class EstatisticasPage {
  private readonly api = inject(EstatisticasService);

  protected readonly visibilidade = signal<VisibilidadeEstatistica>('TODOS');
  protected readonly linhas = signal<EstatisticaComunidade[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly chips: OpcaoChip<VisibilidadeEstatistica>[] = [
    { valor: 'TODOS', rotulo: 'Todos' },
    { valor: 'PUBLICAS', rotulo: 'Públicos' },
    { valor: 'OCULTAS', rotulo: 'Ocultos' },
  ];

  constructor() {
    effect(() => this.carregar(this.visibilidade()));
  }

  private carregar(visibilidade: VisibilidadeEstatistica): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.api.comunidade(visibilidade).subscribe({
      next: (linhas) => { this.linhas.set(linhas); this.carregando.set(false); },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
  }
}
