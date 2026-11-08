import { Component, OnInit, inject, input, signal } from '@angular/core';

import { EstatisticaJogo, EstatisticasService, MediasCategoria } from '../../core/api/estatisticas.service';
import { ErroApi } from '../../core/http/erro-api';

const CATEGORIAS: { chave: keyof MediasCategoria; rotulo: string }[] = [
  { chave: 'graficos', rotulo: 'Gráficos' },
  { chave: 'som', rotulo: 'Som' },
  { chave: 'jogabilidade', rotulo: 'Jogabilidade' },
  { chave: 'diversao', rotulo: 'Diversão' },
  { chave: 'inovacao', rotulo: 'Inovação' },
];

/** Ficha de estatísticas da comunidade exibida na página do jogo (F-17). */
@Component({
  selector: 'app-estatisticas-jogo',
  templateUrl: './estatisticas-jogo.component.html',
  styleUrl: './estatisticas-jogo.component.scss',
})
export class EstatisticasJogoComponent implements OnInit {
  private readonly estatisticasService = inject(EstatisticasService);

  readonly jogoId = input.required<string>();

  protected readonly categorias = CATEGORIAS;
  protected readonly estatisticas = signal<EstatisticaJogo | null>(null);
  protected readonly erroEstatisticas = signal<string | null>(null);

  ngOnInit(): void {
    this.estatisticasService.consultar(this.jogoId()).subscribe({
      next: (estatisticas) => this.estatisticas.set(estatisticas),
      error: (e: ErroApi) => this.erroEstatisticas.set(e.mensagem),
    });
  }

  /** Nota com duas casas e vírgula; categoria sem notas vira um traço. */
  protected nota(valor: number | null): string {
    return valor === null ? '–' : valor.toFixed(2).replace('.', ',');
  }
}
