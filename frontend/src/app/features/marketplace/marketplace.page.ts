import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subject, catchError, debounceTime, forkJoin, of } from 'rxjs';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import {
  Anuncio, ESTADOS, EstadoConservacao, FiltroAnuncios, MIDIAS, MarketplaceService, TIPOS_NEGOCIACAO, TipoNegociacao,
} from '../../core/api/marketplace.service';
import { NegociacaoService } from '../../core/api/negociacao.service';
import { PerfilPublicoService } from '../../core/api/perfil-publico.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';
import { gradientePara } from '../../shared/apresentacao-jogo';
import { AvisosService } from '../../shared/ui/avisos.service';
import { CarregandoComponent, ErroComponent, EstadoVazioComponent } from '../../shared/ui/estados.component';
import { IconeComponent } from '../../shared/ui/icone.component';
import { urlDaImagem } from './imagem';

const FILTRO_VAZIO: Required<FiltroAnuncios> = {
  jogoId: '', titulo: '', precoMinimo: null, precoMaximo: null, estado: '', tipoNegociacao: '', regiao: '',
};

/**
 * Marketplace (F-18): hero, "Anúncios da comunidade" com a linha de filtros inline (aplicados na hora) e cards
 * em 4 colunas. "Comprar" leva ao checkout (F-20) e fica oculto em anúncio só de troca ou do próprio usuário;
 * "Conversar" abre a negociação (F-19).
 */
@Component({
  selector: 'app-marketplace',
  imports: [CurrencyPipe, FormsModule, RouterLink, CarregandoComponent, ErroComponent, EstadoVazioComponent, IconeComponent],
  templateUrl: './marketplace.page.html',
  styleUrl: './marketplace.page.scss',
})
export class MarketplacePage {
  private readonly marketplace = inject(MarketplaceService);
  private readonly negociacao = inject(NegociacaoService);
  private readonly perfis = inject(PerfilPublicoService);
  private readonly avisos = inject(AvisosService);
  private readonly router = inject(Router);
  protected readonly autenticacao = inject(AutenticacaoService);

  protected readonly estados = ESTADOS;
  protected readonly tipos = TIPOS_NEGOCIACAO;
  protected readonly midias = MIDIAS;
  protected readonly anuncios = signal<Anuncio[]>([]);
  protected readonly jogos = signal<Jogo[]>([]);
  protected readonly vendedores = signal<Map<string, string>>(new Map());
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected filtro: Required<FiltroAnuncios> = { ...FILTRO_VAZIO };
  private readonly mudancas = new Subject<void>();

  constructor() {
    inject(CatalogoService).listarJogos().subscribe({ next: (jogos) => this.jogos.set(jogos) });
    this.mudancas.pipe(debounceTime(300), takeUntilDestroyed()).subscribe(() => this.pesquisar());
    this.pesquisar();
  }

  /** Filtros aplicados na hora, sem botão "Buscar". */
  protected filtrar(): void {
    this.mudancas.next();
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

  protected foto(anuncio: Anuncio): string | null {
    return anuncio.fotos.map((f) => urlDaImagem(f)).find(Boolean) ?? null;
  }

  protected fundo(anuncio: Anuncio): string {
    return gradientePara(anuncio.jogoId);
  }

  protected meu(anuncio: Anuncio): boolean {
    return anuncio.vendedorId === this.autenticacao.usuarioAtual()?.id;
  }

  /** "Comprar" só para anúncio ativo, que aceita venda e não é do próprio usuário. */
  protected podeComprar(anuncio: Anuncio): boolean {
    return anuncio.status === 'ATIVO' && anuncio.tipoNegociacao !== 'TROCA' && !this.meu(anuncio);
  }

  protected conversar(anuncio: Anuncio): void {
    if (!this.autenticacao.usuarioAtual()) { this.router.navigate(['/login']); return; }
    this.negociacao.iniciar(anuncio.id).subscribe({
      next: (conversa) => this.router.navigate(['/marketplace/mensagens', conversa.id]),
      error: (e: ErroApi) => this.avisos.erro(e.mensagem),
    });
  }

  protected comprar(anuncio: Anuncio): void {
    if (!this.autenticacao.usuarioAtual()) { this.router.navigate(['/login']); return; }
    this.router.navigate(['/marketplace/checkout'], { queryParams: { anuncio: anuncio.id } });
  }

  protected pesquisar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.marketplace.buscar({ ...this.filtro, titulo: this.filtro.titulo.trim(), regiao: this.filtro.regiao.trim() })
      .subscribe({
        next: (anuncios) => {
          this.anuncios.set(anuncios);
          this.carregando.set(false);
          this.carregarVendedores(anuncios);
        },
        error: (e: ErroApi) => {
          this.erro.set(e.mensagem);
          this.carregando.set(false);
        },
      });
  }

  /** "por @usuário": o nome público de cada vendedor, uma consulta por vendedor. */
  private carregarVendedores(anuncios: Anuncio[]): void {
    const conhecidos = this.vendedores();
    const faltam = [...new Set(anuncios.map((a) => a.vendedorId))].filter((id) => !conhecidos.has(id));
    if (!faltam.length) return;
    forkJoin(faltam.map((id) => this.perfis.consultar(id).pipe(catchError(() => of(null))))).subscribe((perfis) => {
      const novo = new Map(this.vendedores());
      perfis.forEach((p) => { if (p) novo.set(p.id, p.nome); });
      this.vendedores.set(novo);
    });
  }
}
