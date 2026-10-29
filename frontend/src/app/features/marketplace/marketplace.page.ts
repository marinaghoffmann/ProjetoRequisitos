import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import {
  Anuncio,
  ESTADOS,
  EstadoConservacao,
  FiltroAnuncios,
  MIDIAS,
  MarketplaceService,
  TIPOS_NEGOCIACAO,
  TipoNegociacao,
} from '../../core/api/marketplace.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-marketplace',
  imports: [CurrencyPipe, FormsModule, RouterLink],
  templateUrl: './marketplace.page.html',
  styleUrl: './marketplace.page.scss',
})
export class MarketplacePage {
  private readonly marketplace = inject(MarketplaceService);
  private readonly catalogo = inject(CatalogoService);
  protected readonly autenticacao = inject(AutenticacaoService);

  protected readonly estados = ESTADOS;
  protected readonly tipos = TIPOS_NEGOCIACAO;
  protected readonly midias = MIDIAS;
  protected readonly anuncios = signal<Anuncio[]>([]);
  protected readonly jogos = signal<Jogo[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected filtro: Required<FiltroAnuncios> = {
    jogoId: '',
    titulo: '',
    precoMinimo: null,
    precoMaximo: null,
    estado: '',
    tipoNegociacao: '',
    regiao: '',
  };

  constructor() {
    this.catalogo.listarJogos().subscribe({ next: (jogos) => this.jogos.set(jogos) });
    this.pesquisar();
  }

  protected chaves<T extends string>(opcoes: Record<T, string>): T[] {
    return Object.keys(opcoes) as T[];
  }

  protected rotuloEstado(estado: EstadoConservacao): string {
    return this.estados[estado];
  }

  protected rotuloTipo(tipo: TipoNegociacao): string {
    return this.tipos[tipo];
  }

  protected imagemUrl(caminho: string): string {
    return caminho.startsWith('/uploads/') || /^https?:\/\//.test(caminho) ? caminho : '';
  }

  protected limpar(): void {
    this.filtro = {
      jogoId: '',
      titulo: '',
      precoMinimo: null,
      precoMaximo: null,
      estado: '',
      tipoNegociacao: '',
      regiao: '',
    };
    this.pesquisar();
  }

  protected pesquisar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.marketplace.buscar({ ...this.filtro, titulo: this.filtro.titulo.trim(), regiao: this.filtro.regiao.trim() })
      .subscribe({
        next: (anuncios) => {
          this.anuncios.set(anuncios);
          this.carregando.set(false);
        },
        error: (e: ErroApi) => {
          this.erro.set(e.mensagem);
          this.carregando.set(false);
        },
      });
  }
}
