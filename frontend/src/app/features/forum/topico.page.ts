import { Component, computed, inject, signal } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ForumService, RespostaForum, STATUS_TOPICO, StatusTopico, Topico } from '../../core/api/forum.service';
import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';
import { urlDaImagem } from '../marketplace/imagem';
import { AnexoImagens } from './anexo-imagens.component';
import { formatarMarkdownForum } from './markdown-forum';

/**
 * Tópico com as respostas em árvore. Os botões de editar e excluir só aparecem para o autor e,
 * no caso da edição, dentro das 48 horas; quem decide de fato é o backend.
 */
@Component({
  selector: 'app-topico',
  imports: [AnexoImagens, FormsModule, NgTemplateOutlet, RouterLink],
  templateUrl: './topico.page.html',
  styleUrl: './topico.page.scss',
})
export class TopicoPage {
  private readonly forum = inject(ForumService);
  private readonly catalogo = inject(CatalogoService);
  private readonly roteador = inject(Router);
  protected readonly autenticacao = inject(AutenticacaoService);
  private readonly id = inject(ActivatedRoute).snapshot.paramMap.get('id')!;

  protected readonly status = STATUS_TOPICO;
  protected readonly markdown = formatarMarkdownForum;
  protected readonly urlDaImagem = urlDaImagem;
  protected readonly topico = signal<Topico | null>(null);
  protected readonly jogos = signal<Jogo[]>([]);
  protected readonly carregando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  /** Resposta que está recebendo uma réplica, ou null quando o formulário principal está em uso. */
  protected readonly respondendoA = signal<string | null>(null);
  protected readonly editando = signal<string | null>(null);
  /** Imagens já enviadas e ainda não publicadas, uma lista por formulário. */
  protected readonly imagensNovaResposta = signal<string[]>([]);
  protected readonly imagensReplica = signal<string[]>([]);
  /** true enquanto alguma imagem está subindo: o envio da resposta espera. */
  protected readonly anexando = signal(false);
  protected novaResposta = '';
  protected replica = '';
  protected textoEditado = '';

  /** Respostas agrupadas pela resposta pai; a chave '' reúne as respostas diretas ao tópico. */
  protected readonly filhas = computed(() => {
    const grupos = new Map<string, RespostaForum[]>();
    for (const resposta of this.topico()?.respostas ?? []) {
      const chave = resposta.respostaPaiId ?? '';
      grupos.set(chave, [...(grupos.get(chave) ?? []), resposta]);
    }
    return grupos;
  });

  constructor() {
    this.catalogo.listarJogos().subscribe({ next: (jogos) => this.jogos.set(jogos) });
    this.forum.buscarTopico(this.id).subscribe({
      next: (topico) => this.atualizar(topico),
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }

  protected encerrado(): boolean {
    return this.topico()?.status === 'ENCERRADO';
  }

  protected ehAutor(autorId: string): boolean {
    return this.autenticacao.usuarioAtual()?.id === autorId;
  }

  protected iniciais(nome: string | null): string {
    return (nome ?? '?').trim().slice(0, 2).toUpperCase();
  }

  protected corDoAvatar(id: string): string {
    const cores = ['#9752c8', '#487db5', '#bf6695', '#557f80', '#ba7c46'];
    return cores[id.charCodeAt(0) % cores.length];
  }

  protected avatarDe(url: string | null): string | null {
    return url ? `url("${encodeURI(url)}") center / cover no-repeat` : null;
  }

  protected tituloDoJogo(jogoId: string | null): string {
    return jogoId ? (this.jogos().find((jogo) => jogo.id === jogoId)?.titulo ?? 'Jogo') : 'Geral';
  }

  protected categoria(categoria: string): string {
    return categoria === 'Geral' ? 'Discussão geral' : categoria;
  }

  protected papel(papel: string | null): string {
    return ({ COMUM: 'Jogador', DESENVOLVEDOR: 'Desenvolvedor', MODERADOR: 'Moderador',
      ADMINISTRADOR: 'Administrador' } as Record<string, string>)[papel ?? ''] ?? 'Membro';
  }

  protected tempoRelativo(data: string): string {
    const segundos = Math.round((Date.parse(data) - Date.now()) / 1000);
    const absoluto = Math.abs(segundos);
    if (absoluto < 60) return 'agora';
    const unidade: Intl.RelativeTimeFormatUnit = absoluto < 3600 ? 'minute' : absoluto < 86400 ? 'hour' : 'day';
    const divisor = unidade === 'minute' ? 60 : unidade === 'hour' ? 3600 : 86400;
    return new Intl.RelativeTimeFormat('pt-BR', { numeric: 'auto' }).format(Math.round(segundos / divisor), unidade);
  }

  protected formatar(campo: HTMLTextAreaElement, alvo: 'novaResposta' | 'replica' | 'textoEditado',
    tipo: 'negrito' | 'italico' | 'codigo' | 'citacao' | 'lista'): void {
    const valor = this[alvo];
    const selecionado = valor.slice(campo.selectionStart, campo.selectionEnd) || 'texto';
    const trecho = ({ negrito: `**${selecionado}**`, italico: `*${selecionado}*`,
      codigo: `\`${selecionado}\``, citacao: `> ${selecionado}`, lista: `- ${selecionado}` })[tipo];
    this[alvo] = valor.slice(0, campo.selectionStart) + trecho + valor.slice(campo.selectionEnd);
    campo.focus();
  }

  protected curtir(respostaId?: string): void {
    const atual = this.topico();
    if (!atual || this.enviando()) return;
    const curtido = respostaId
      ? atual.respostas.find((resposta) => resposta.id === respostaId)?.curtidaPeloUsuario ?? false
      : atual.curtidoPeloUsuario;
    this.enviando.set(true);
    this.erro.set(null);
    this.forum.definirCurtida(this.id, !curtido, respostaId).subscribe({
      next: () => this.forum.buscarTopico(this.id).subscribe({
        next: (topico) => this.atualizar(topico),
        error: (e: ErroApi) => { this.erro.set(e.mensagem); this.enviando.set(false); },
      }),
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.enviando.set(false); },
    });
  }

  protected dentroDoPrazo(editavelAte: string): boolean {
    return Date.now() <= Date.parse(editavelAte);
  }

  protected responder(): void {
    this.enviar(this.novaResposta, null, this.imagensNovaResposta(), () => {
      this.novaResposta = '';
      this.imagensNovaResposta.set([]);
    });
  }

  protected abrirReplica(resposta: RespostaForum): void {
    this.editando.set(null);
    this.replica = '';
    this.imagensReplica.set([]);
    this.respondendoA.set(resposta.id);
  }

  protected enviarReplica(resposta: RespostaForum): void {
    this.enviar(this.replica, resposta.id, this.imagensReplica(), () => {
      this.replica = '';
      this.imagensReplica.set([]);
      this.respondendoA.set(null);
    });
  }

  protected abrirEdicao(resposta: RespostaForum): void {
    this.respondendoA.set(null);
    this.textoEditado = resposta.conteudo ?? '';
    this.editando.set(resposta.id);
  }

  protected salvarEdicao(resposta: RespostaForum): void {
    this.executar(this.forum.editarResposta(this.id, resposta.id, this.textoEditado), () => this.editando.set(null));
  }

  protected excluirResposta(resposta: RespostaForum): void {
    if (!confirm('Excluir a sua resposta? As respostas a ela continuam visíveis.')) return;
    this.executar(this.forum.excluirResposta(this.id, resposta.id));
  }

  protected alterarStatus(novo: StatusTopico): void {
    if (novo === 'ENCERRADO' && !confirm('Encerrar o tópico? Ninguém mais poderá responder nem editar.')) return;
    this.executar(this.forum.alterarStatus(this.id, novo));
  }

  protected excluirTopico(): void {
    if (!confirm('Excluir o tópico? Ele deixará de aparecer no fórum.')) return;
    this.forum.excluir(this.id).subscribe({
      next: () => this.roteador.navigate(['/forum']),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  private enviar(texto: string, respostaPaiId: string | null, imagens: string[], aoConcluir: () => void): void {
    if (!texto.trim() || this.anexando()) return;
    this.executar(this.forum.responder(this.id, texto, respostaPaiId, imagens), aoConcluir);
  }

  private executar(requisicao: ReturnType<ForumService['buscarTopico']>, aoConcluir?: () => void): void {
    this.enviando.set(true);
    this.erro.set(null);
    requisicao.subscribe({
      next: (topico) => {
        this.atualizar(topico);
        aoConcluir?.();
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.enviando.set(false);
      },
    });
  }

  private atualizar(topico: Topico): void {
    this.topico.set(topico);
    this.carregando.set(false);
    this.enviando.set(false);
  }
}