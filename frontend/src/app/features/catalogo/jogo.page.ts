import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { EstatisticaJogo, EstatisticasService, MediasCategoria } from '../../core/api/estatisticas.service';
import { ErroApi } from '../../core/http/erro-api';

const CATEGORIAS: { chave: keyof MediasCategoria; rotulo: string }[] = [
  { chave: 'graficos', rotulo: 'Gráficos' },
  { chave: 'som', rotulo: 'Som' },
  { chave: 'jogabilidade', rotulo: 'Jogabilidade' },
  { chave: 'diversao', rotulo: 'Diversão' },
  { chave: 'inovacao', rotulo: 'Inovação' },
];

/** Página do jogo com a ficha de estatísticas da comunidade (F-17). */
@Component({
  selector: 'app-jogo',
  imports: [DatePipe, RouterLink],
  templateUrl: './jogo.page.html',
  styleUrl: './jogo.page.scss',
})
export class JogoPage {
  private readonly catalogo = inject(CatalogoService);
  private readonly estatisticasService = inject(EstatisticasService);

  protected readonly categorias = CATEGORIAS;
  protected readonly jogo = signal<Jogo | null>(null);
  protected readonly estatisticas = signal<EstatisticaJogo | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroEstatisticas = signal<string | null>(null);

  constructor() {
    const id = inject(ActivatedRoute).snapshot.paramMap.get('id') ?? '';
    this.catalogo.buscarJogo(id).subscribe({
      next: (jogo) => {
        this.jogo.set(jogo);
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
    // Uma falha nas estatísticas não deve esconder os dados do jogo.
    this.estatisticasService.consultar(id).subscribe({
      next: (estatisticas) => this.estatisticas.set(estatisticas),
      error: (e: ErroApi) => this.erroEstatisticas.set(e.mensagem),
    });
  }

  /** Nota com duas casas e vírgula; categoria sem notas vira um traço. */
  protected nota(valor: number | null): string {
    return valor === null ? '–' : valor.toFixed(2).replace('.', ',');
  }

  protected imagemUrl(caminho: string): string {
    if (caminho.startsWith('/uploads/')) return caminho;
    if (caminho.startsWith('http://') || caminho.startsWith('https://')) return caminho;
    return '';
  }
}
