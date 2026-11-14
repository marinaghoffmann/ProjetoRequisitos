import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, NgTemplateOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ForumService, RespostaForum, STATUS_TOPICO, StatusTopico, Topico } from '../../core/api/forum.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

/**
 * Tópico com as respostas em árvore. Os botões de editar e excluir só aparecem para o autor e,
 * no caso da edição, dentro das 48 horas; quem decide de fato é o backend.
 */
@Component({
  selector: 'app-topico',
  imports: [DatePipe, FormsModule, NgTemplateOutlet, RouterLink],
  templateUrl: './topico.page.html',
  styleUrl: './topico.page.scss',
})
export class TopicoPage {
  private readonly forum = inject(ForumService);
  private readonly roteador = inject(Router);
  protected readonly autenticacao = inject(AutenticacaoService);
  private readonly id = inject(ActivatedRoute).snapshot.paramMap.get('id')!;

  protected readonly status = STATUS_TOPICO;
  protected readonly topico = signal<Topico | null>(null);
  protected readonly carregando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  /** Resposta que está recebendo uma réplica, ou null quando o formulário principal está em uso. */
  protected readonly respondendoA = signal<string | null>(null);
  protected readonly editando = signal<string | null>(null);
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

  protected dentroDoPrazo(editavelAte: string): boolean {
    return Date.now() <= Date.parse(editavelAte);
  }

  protected responder(): void {
    this.enviar(this.novaResposta, null, () => (this.novaResposta = ''));
  }

  protected abrirReplica(resposta: RespostaForum): void {
    this.editando.set(null);
    this.replica = '';
    this.respondendoA.set(resposta.id);
  }

  protected enviarReplica(resposta: RespostaForum): void {
    this.enviar(this.replica, resposta.id, () => {
      this.replica = '';
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

  private enviar(texto: string, respostaPaiId: string | null, aoConcluir: () => void): void {
    if (!texto.trim()) return;
    this.executar(this.forum.responder(this.id, texto, respostaPaiId), aoConcluir);
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
