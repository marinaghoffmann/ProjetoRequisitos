import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-catalogo',
  imports: [DatePipe, FormsModule, RouterLink],
  templateUrl: './catalogo.page.html',
  styleUrl: './catalogo.page.scss',
})
export class CatalogoPage {
  private readonly catalogo = inject(CatalogoService);
  protected readonly autenticacao = inject(AutenticacaoService);

  protected readonly jogos = signal<Jogo[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected busca = '';

  constructor() {
    this.carregar();
  }

  protected pesquisar(): void {
    this.carregar(this.busca.trim() || undefined);
  }

  protected imagemUrl(caminho: string): string {
    if (caminho.startsWith('/uploads/')) return caminho;
    if (caminho.startsWith('http://') || caminho.startsWith('https://')) return caminho;
    return '';
  }

  protected podeEditar(jogo: Jogo): boolean {
    const usuario = this.autenticacao.usuarioAtual();
    if (!usuario) return false;
    return usuario.papel === 'ADMINISTRADOR'
      || (usuario.papel === 'DESENVOLVEDOR' && usuario.desenvolvedoraId === jogo.desenvolvedoraId);
  }

  protected excluir(jogo: Jogo): void {
    if (!confirm(`Desativar "${jogo.titulo}" do catálogo?`)) return;
    this.catalogo.excluirJogo(jogo.id).subscribe({
      next: () => this.pesquisar(),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  private carregar(titulo?: string): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.catalogo.listarJogos(titulo).subscribe({
      next: (jogos) => {
        this.jogos.set(jogos);
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }
}
