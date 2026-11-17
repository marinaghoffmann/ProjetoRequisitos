import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { ForumService, Pagina, ResumoTopico, STATUS_TOPICO, StatusTopico } from '../../core/api/forum.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';
import { AnexoImagens } from './anexo-imagens.component';
import { formatarMarkdownForum } from './markdown-forum';

@Component({
  selector: 'app-forum',
  imports: [FormsModule, RouterLink, AnexoImagens],
  templateUrl: './forum.page.html',
  styleUrl: './forum.page.scss',
})
export class ForumPage {
  private readonly forum = inject(ForumService);
  private readonly catalogo = inject(CatalogoService);
  private readonly roteador = inject(Router);
  protected readonly autenticacao = inject(AutenticacaoService);
  protected readonly status = STATUS_TOPICO;
  protected readonly markdown = formatarMarkdownForum;
  protected readonly categorias = ['Todas', 'Discussão geral', 'Recomendações', 'Ajuda', 'Spoilers', 'Anúncios'];
  protected readonly estados: Array<{ valor: StatusTopico | ''; label: string }> = [
    { valor: '', label: 'Todos' }, { valor: 'ABERTO', label: 'Aberto' },
    { valor: 'RESOLVIDO', label: 'Resolvido' }, { valor: 'ENCERRADO', label: 'Encerrado' },
  ];
  protected readonly jogos = signal<Jogo[]>([]);
  protected readonly pagina = signal<Pagina<ResumoTopico> | null>(null);
  protected readonly carregando = signal(true);
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly editorAberto = signal(false);
  protected readonly imagens = signal<string[]>([]);
  protected readonly anexando = signal(false);
  protected readonly tamanho = 5;
  protected filtro = { jogoId: '', categoria: 'Todas', status: '' as StatusTopico | '', ordem: 'recentes' as 'recentes' | 'emAlta' };
  protected rascunho = { jogoId: '', categoria: 'Discussão geral', titulo: '', conteudo: '' };
  private readonly valorGerais = '__GERAIS__';

  constructor() {
    this.catalogo.listarJogos().subscribe({ next: (jogos) => this.jogos.set(jogos) });
    this.carregar(1);
  }

  protected tituloDoJogo(jogoId: string | null): string {
    return jogoId ? (this.jogos().find((jogo) => jogo.id === jogoId)?.titulo ?? 'Jogo') : 'Geral';
  }

  protected categoriaDoTopico(categoria: string): string {
    return categoria === 'Geral' ? 'Discussão geral' : categoria;
  }

  protected iniciais(nome: string | null): string {
    return (nome ?? '?').trim().split(/\s+/).slice(0, 2).map((parte) => parte[0]).join('').toUpperCase();
  }

  protected corDoAvatar(id: string): string {
    const cores = ['#9752c8', '#487db5', '#bf6695', '#557f80', '#ba7c46'];
    return cores[id.charCodeAt(0) % cores.length];
  }

  protected avatarDe(url: string | null): string | null {
    return url ? `url("${encodeURI(url)}") center / cover no-repeat` : null;
  }

  protected tempoRelativo(data: string): string {
    const segundos = Math.round((Date.parse(data) - Date.now()) / 1000);
    const absoluto = Math.abs(segundos);
    if (absoluto < 60) return 'agora';
    const unidade: Intl.RelativeTimeFormatUnit = absoluto < 3600 ? 'minute' : absoluto < 86400 ? 'hour' : 'day';
    const divisor = unidade === 'minute' ? 60 : unidade === 'hour' ? 3600 : 86400;
    return new Intl.RelativeTimeFormat('pt-BR', { numeric: 'auto' }).format(Math.round(segundos / divisor), unidade);
  }

  protected escolherCategoria(categoria: string): void { this.filtro.categoria = categoria; this.carregar(1); }
  protected escolherStatus(status: StatusTopico | ''): void { this.filtro.status = status; this.carregar(1); }
  protected escolherOrdem(ordem: 'recentes' | 'emAlta'): void { this.filtro.ordem = ordem; this.carregar(1); }
  protected escolherJogo(): void { this.carregar(1); }

  protected abrirEditor(): void {
    if (!this.autenticacao.usuarioAtual()) { this.roteador.navigate(['/login']); return; }
    this.editorAberto.update((aberto) => !aberto);
    this.erro.set(null);
  }

  protected formatar(campo: HTMLTextAreaElement, tipo: 'negrito' | 'italico' | 'codigo' | 'citacao' | 'lista'): void {
    const antes = this.rascunho.conteudo.slice(0, campo.selectionStart);
    const selecionado = this.rascunho.conteudo.slice(campo.selectionStart, campo.selectionEnd) || 'texto';
    const depois = this.rascunho.conteudo.slice(campo.selectionEnd);
    const trecho = ({ negrito: `**${selecionado}**`, italico: `*${selecionado}*`,
      codigo: `\`${selecionado}\``, citacao: `> ${selecionado}`, lista: `- ${selecionado}` })[tipo];
    this.rascunho.conteudo = antes + trecho + depois;
    campo.focus();
  }

  protected publicar(): void {
    if (!this.rascunho.titulo.trim() || !this.rascunho.conteudo.trim() || this.salvando() || this.anexando()) return;
    this.salvando.set(true);
    this.erro.set(null);
    this.forum.criar({ ...this.rascunho, jogoId: this.rascunho.jogoId || null, imagens: this.imagens() }).subscribe({
      next: (topico) => this.roteador.navigate(['/forum', topico.id]),
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.salvando.set(false); },
    });
  }

  protected carregar(numero: number): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.forum.listar({
      jogoId: this.filtro.jogoId === this.valorGerais ? undefined : this.filtro.jogoId || undefined,
      apenasGerais: this.filtro.jogoId === this.valorGerais || undefined,
      categoria: this.filtro.categoria === 'Todas' ? undefined : this.filtro.categoria,
      status: this.filtro.status || undefined, ordem: this.filtro.ordem,
      pagina: numero, tamanho: this.tamanho,
    }).subscribe({
      next: (pagina) => { this.pagina.set(pagina); this.carregando.set(false); },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
  }

  protected paginas(total: number, atual: number): number[] {
    const inicio = Math.max(1, Math.min(atual - 2, total - 4));
    return Array.from({ length: Math.min(total, 5) }, (_, indice) => inicio + indice);
  }
  protected inicioDaPagina(resultado: Pagina<ResumoTopico>): number {
    return resultado.totalItens ? (resultado.pagina - 1) * resultado.tamanho + 1 : 0;
  }
  protected fimDaPagina(resultado: Pagina<ResumoTopico>): number {
    return Math.min(resultado.pagina * resultado.tamanho, resultado.totalItens);
  }
}
