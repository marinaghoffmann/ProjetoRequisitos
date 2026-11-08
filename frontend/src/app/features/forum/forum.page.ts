import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { ForumService, Pagina, ResumoTopico, STATUS_TOPICO } from '../../core/api/forum.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

/** Lista paginada de tópicos, por jogo ou categoria, do mais recentemente ativo para o menos. */
@Component({
  selector: 'app-forum',
  imports: [DatePipe, FormsModule, RouterLink],
  templateUrl: './forum.page.html',
  styleUrl: './forum.page.scss',
})
export class ForumPage {
  private readonly forum = inject(ForumService);
  private readonly catalogo = inject(CatalogoService);
  protected readonly autenticacao = inject(AutenticacaoService);

  protected readonly status = STATUS_TOPICO;
  protected readonly tamanho = 10;
  protected readonly jogos = signal<Jogo[]>([]);
  protected readonly pagina = signal<Pagina<ResumoTopico> | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected filtro = { jogoId: '', categoria: '' };

  constructor() {
    this.catalogo.listarJogos().subscribe({ next: (jogos) => this.jogos.set(jogos) });
    this.carregar(1);
  }

  protected tituloDoJogo(jogoId: string | null): string | null {
    return jogoId ? (this.jogos().find((jogo) => jogo.id === jogoId)?.titulo ?? null) : null;
  }

  protected pesquisar(): void {
    this.carregar(1);
  }

  protected limpar(): void {
    this.filtro = { jogoId: '', categoria: '' };
    this.carregar(1);
  }

  protected carregar(numero: number): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.forum
      .listar({ jogoId: this.filtro.jogoId, categoria: this.filtro.categoria.trim(), pagina: numero, tamanho: this.tamanho })
      .subscribe({
        next: (pagina) => {
          this.pagina.set(pagina);
          this.carregando.set(false);
        },
        error: (e: ErroApi) => {
          this.erro.set(e.mensagem);
          this.carregando.set(false);
        },
      });
  }
}
