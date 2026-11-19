import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { JogoNaVitrine, VitrineService } from '../../core/catalogo/vitrine.service';
import { ErroApi } from '../../core/http/erro-api';
import { CabecalhoPaginaComponent } from '../../shared/ui/cabecalho-pagina.component';
import { CardPosterComponent, DadosPoster } from '../../shared/ui/card-poster.component';
import { ChipsComponent, OpcaoChip } from '../../shared/ui/chips.component';
import { CarregandoComponent, ErroComponent, EstadoVazioComponent } from '../../shared/ui/estados.component';
import { AvisosService } from '../../shared/ui/avisos.service';
import { OPCOES_DE_ORDENACAO, Ordenacao, ordenar } from './ordenacao';

const TODOS = '';

/**
 * Descobrir: a vitrine curada do catálogo. Chips de gênero e plataforma (filtram na hora) e ordenação pela nota
 * da comunidade (F-17). Usa só dados reais: o catálogo, a ficha pública e a wishlist do usuário.
 */
@Component({
  selector: 'app-descobrir',
  imports: [FormsModule, CabecalhoPaginaComponent, CardPosterComponent, ChipsComponent, CarregandoComponent,
    ErroComponent, EstadoVazioComponent],
  templateUrl: './descobrir.page.html',
  styleUrl: './descobrir.page.scss',
})
export class DescobrirPage {
  private readonly vitrine = inject(VitrineService);
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly avisos = inject(AvisosService);
  private readonly router = inject(Router);

  protected readonly jogos = signal<JogoNaVitrine[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly filtro = signal<string>(TODOS);
  protected readonly ordem = signal<Ordenacao>('MELHOR_AVALIADOS');
  protected readonly opcoesDeOrdenacao = OPCOES_DE_ORDENACAO;

  /** "Todos", os gêneros e as plataformas que de fato aparecem no catálogo. */
  protected readonly chips = computed<OpcaoChip[]>(() => {
    const generos = new Set(this.jogos().flatMap((j) => j.generos));
    const plataformas = new Set(this.jogos().flatMap((j) => j.plataformas));
    return [
      { valor: TODOS, rotulo: 'Todos' },
      ...[...generos].sort().map((g) => ({ valor: `g:${g}`, rotulo: g })),
      ...[...plataformas].sort().map((p) => ({ valor: `p:${p}`, rotulo: p })),
    ];
  });

  protected readonly visiveis = computed(() => {
    const filtro = this.filtro();
    const filtrados = this.jogos().filter((j) => {
      if (!filtro) return true;
      const [tipo, valor] = [filtro.slice(0, 2), filtro.slice(2)];
      return tipo === 'g:' ? j.generos.includes(valor) : j.plataformas.includes(valor);
    });
    return ordenar(filtrados, this.ordem());
  });

  constructor() {
    this.vitrine.carregar().subscribe({
      next: (jogos) => { this.jogos.set(jogos); this.carregando.set(false); },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
  }

  protected alternarWishlist(poster: DadosPoster): void {
    if (!this.autenticacao.usuarioAtual()) {
      this.router.navigate(['/login']);
      return;
    }
    const atual = !!poster.naWishlist;
    this.vitrine.alternarWishlist(poster.id, atual).subscribe({
      next: (agora) => {
        this.jogos.update((lista) => lista.map((j) => (j.id === poster.id ? { ...j, naWishlist: agora } : j)));
        this.avisos.sucesso(agora ? `${poster.titulo} entrou na sua wishlist` : `${poster.titulo} saiu da sua wishlist`);
      },
      error: (e: ErroApi) => this.avisos.erro(e.mensagem),
    });
  }
}
