import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { CatalogoService, Desenvolvedora, FiltroCatalogo, OrigemJogo, StatusCatalogo } from '../../core/api/catalogo.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { JogoNaVitrine, VitrineService } from '../../core/catalogo/vitrine.service';
import { ErroApi } from '../../core/http/erro-api';
import { AvisosService } from '../../shared/ui/avisos.service';
import { CabecalhoPaginaComponent } from '../../shared/ui/cabecalho-pagina.component';
import { CardPosterComponent, DadosPoster } from '../../shared/ui/card-poster.component';
import { ChipsComponent, OpcaoChip } from '../../shared/ui/chips.component';
import { CarregandoComponent, ErroComponent, EstadoVazioComponent } from '../../shared/ui/estados.component';
import { OPCOES_DE_ORDENACAO, Ordenacao, ordenar } from '../descobrir/ordenacao';

/**
 * "Todos os jogos" (F-01): a mesma grade de pôsteres da Descobrir, com os filtros da consulta do catálogo
 * (título, gênero, desenvolvedora, origem e, para o administrador, status). Desenvolvedora, origem e status vão
 * para a API; título e gênero filtram na hora sobre o que veio.
 */
@Component({
  selector: 'app-catalogo',
  imports: [FormsModule, RouterLink, CabecalhoPaginaComponent, CardPosterComponent, ChipsComponent,
    CarregandoComponent, ErroComponent, EstadoVazioComponent],
  templateUrl: './catalogo.page.html',
  styleUrl: './catalogo.page.scss',
})
export class CatalogoPage {
  private readonly catalogo = inject(CatalogoService);
  private readonly vitrine = inject(VitrineService);
  private readonly avisos = inject(AvisosService);
  private readonly router = inject(Router);
  protected readonly autenticacao = inject(AutenticacaoService);

  private readonly rota = inject(ActivatedRoute);

  /** "Gerenciar catálogo" no menu abre esta tela com ?gerenciar=1. */
  protected readonly gerenciando = toSignal(
    this.rota.queryParamMap.pipe(map((p) => p.get('gerenciar') === '1')), { initialValue: false });

  protected readonly jogos = signal<JogoNaVitrine[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly desenvolvedoras = toSignal(
    this.catalogo.listarDesenvolvedoras().pipe(catchError(() => of([] as Desenvolvedora[]))), { initialValue: [] });

  protected readonly titulo = signal('');
  /** ?genero=Nome vem dos atalhos da busca. */
  protected readonly genero = signal(this.rota.snapshot.queryParamMap.get('genero') ?? '');
  protected readonly desenvolvedoraId = signal('');
  protected readonly origem = signal<OrigemJogo | ''>('');
  protected readonly status = signal<StatusCatalogo>('ATIVO');
  protected readonly ordem = signal<Ordenacao>('TITULO');
  protected readonly opcoesDeOrdenacao = OPCOES_DE_ORDENACAO;

  protected readonly chipsDeGenero = computed<OpcaoChip[]>(() => [
    { valor: '', rotulo: 'Todos' },
    ...[...new Set(this.jogos().flatMap((j) => j.generos))].sort().map((g) => ({ valor: g, rotulo: g })),
  ]);

  protected readonly visiveis = computed(() => {
    const termo = this.titulo().trim().toLocaleLowerCase('pt-BR');
    const genero = this.genero();
    return ordenar(this.jogos().filter((j) => (!termo || j.titulo.toLocaleLowerCase('pt-BR').includes(termo))
      && (!genero || j.generos.includes(genero))), this.ordem());
  });

  constructor() {
    this.carregar();
  }

  protected podeCadastrar(): boolean {
    return this.autenticacao.temPapel('DESENVOLVEDOR', 'ADMINISTRADOR');
  }

  protected podeEditar(jogo: JogoNaVitrine): boolean {
    const usuario = this.autenticacao.usuarioAtual();
    if (!usuario) return false;
    return usuario.papel === 'ADMINISTRADOR'
      || (usuario.papel === 'DESENVOLVEDOR' && usuario.desenvolvedoraId === jogo.jogo.desenvolvedoraId);
  }

  /** Recarrega da API quando muda um filtro que a API conhece (desenvolvedora, origem, status). */
  protected carregar(): void {
    const filtro: FiltroCatalogo = {
      desenvolvedoraId: this.desenvolvedoraId() || undefined,
      origem: this.origem() || undefined,
      status: this.autenticacao.temPapel('ADMINISTRADOR') ? this.status() : undefined,
    };
    this.carregando.set(true);
    this.erro.set(null);
    this.vitrine.carregar(filtro).subscribe({
      next: (jogos) => { this.jogos.set(jogos); this.carregando.set(false); },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
  }

  /** F-01 RN 3: definitiva sem vínculos, lógica (fica inativo) com vínculos. A API diz qual aconteceu. */
  protected async excluir(jogo: JogoNaVitrine): Promise<void> {
    const confirmado = await this.avisos.confirmar({
      titulo: 'Remover jogo do catálogo',
      mensagem: `Remover "${jogo.titulo}"? Se houver reviews, listas ou outros vínculos, ele só fica inativo.`,
      confirmar: 'Remover', perigo: true,
    });
    if (!confirmado) return;
    this.catalogo.excluirJogo(jogo.id).subscribe({
      next: (r) => {
        this.avisos.sucesso(r.exclusao === 'DEFINITIVA'
          ? `${r.titulo} foi excluído do catálogo` : `${r.titulo} tem vínculos e ficou inativo`);
        this.carregar();
      },
      error: (e: ErroApi) => this.avisos.erro(e.mensagem),
    });
  }

  protected alternarWishlist(poster: DadosPoster): void {
    if (!this.autenticacao.usuarioAtual()) {
      this.router.navigate(['/login']);
      return;
    }
    this.vitrine.alternarWishlist(poster.id, !!poster.naWishlist).subscribe({
      next: (agora) => {
        this.jogos.update((lista) => lista.map((j) => (j.id === poster.id ? { ...j, naWishlist: agora } : j)));
        this.avisos.sucesso(agora ? `${poster.titulo} entrou na sua wishlist` : `${poster.titulo} saiu da sua wishlist`);
      },
      error: (e: ErroApi) => this.avisos.erro(e.mensagem),
    });
  }
}
