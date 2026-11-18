import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AcaoModeracao, DenunciaModeracao, LogModeracao, ModeracaoService, PreviaConteudo, TipoConteudo } from '../../core/api/moderacao.service';
import { ErroApi } from '../../core/http/erro-api';

type AlvoSelecionado = { tipo: TipoConteudo; id: string; denunciaId?: string };

@Component({
  selector: 'app-moderacao',
  imports: [DatePipe, FormsModule, RouterLink],
  templateUrl: './moderacao.page.html',
  styleUrl: './moderacao.page.scss',
})
export class ModeracaoPage {
  private readonly api = inject(ModeracaoService);
  protected readonly fila = signal<DenunciaModeracao[]>([]);
  protected readonly logs = signal<LogModeracao[]>([]);
  protected readonly totalFila = signal(0);
  protected readonly totalLogs = signal(0);
  protected readonly paginaFila = signal(1);
  protected readonly paginaLogs = signal(1);
  protected readonly selecionado = signal<AlvoSelecionado | null>(null);
  protected readonly previa = signal<PreviaConteudo | null>(null);
  protected readonly avisos = signal<number | null>(null);
  protected readonly carregando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly mensagem = signal<string | null>(null);
  protected acao: AcaoModeracao = 'OCULTAR';
  protected justificativa = '';
  protected novoTitulo = '';
  protected novoConteudo = '';
  protected novaCategoria = '';
  protected novaRespostaPaiId = '';
  protected motivoAdvertencia = '';
  protected readonly rotulos: Record<TipoConteudo, string> = {
    TOPICO: 'Tópico', RESPOSTA: 'Resposta', REVIEW: 'Review', LISTA: 'Lista',
    AVALIACAO_MARKETPLACE: 'Avaliação de vendedor', ANUNCIO: 'Anúncio', PERFIL: 'Perfil',
  };
  protected readonly acoesGerais: AcaoModeracao[] = ['OCULTAR', 'EXCLUIR', 'MARCAR_18', 'RETIRAR_18', 'REVERTER_OCULTACAO', 'REVERTER_EXCLUSAO'];

  constructor() { this.atualizar(); }

  protected acoes(tipo: TipoConteudo): AcaoModeracao[] {
    return tipo === 'TOPICO' ? [...this.acoesGerais, 'EDITAR', 'MOVER', 'ENCERRAR_TOPICO']
      : tipo === 'RESPOSTA' ? [...this.acoesGerais, 'EDITAR', 'MOVER'] : this.acoesGerais;
  }

  protected atualizar(): void {
    this.carregando.set(true); this.erro.set(null);
    this.api.fila(this.paginaFila()).subscribe({
      next: (p) => { this.fila.set(p.content); this.totalFila.set(p.totalElements); this.carregando.set(false); },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
    this.api.logs(this.paginaLogs()).subscribe({
      next: (p) => { this.logs.set(p.content); this.totalLogs.set(p.totalElements); },
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  protected selecionar(tipo: TipoConteudo, id: string, denunciaId?: string): void {
    this.selecionado.set({ tipo, id, denunciaId }); this.previa.set(null); this.avisos.set(null);
    this.mensagem.set(null); this.erro.set(null); this.acao = 'OCULTAR'; this.justificativa = '';
    this.api.previa(tipo, id).subscribe({
      next: (p) => { this.previa.set(p); this.api.totalAdvertencias(p.autorId).subscribe({
        next: (r) => this.avisos.set(r.total), error: () => this.avisos.set(null),
      }); },
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  protected decidir(): void {
    const alvo = this.selecionado(); if (!alvo) return;
    this.enviando.set(true); this.erro.set(null);
    this.api.agir(this.acao, alvo.tipo, alvo.id, this.justificativa,
      { novoTitulo: this.novoTitulo, novoConteudo: this.novoConteudo, novaCategoria: this.novaCategoria,
        novaRespostaPaiId: this.novaRespostaPaiId.trim() || null }).subscribe({
      next: () => { this.enviando.set(false); this.mensagem.set('Ação registrada no histórico.'); this.atualizar(); },
      error: (e: ErroApi) => { this.enviando.set(false); this.erro.set(e.mensagem); },
    });
  }

  protected arquivar(): void {
    const id = this.selecionado()?.denunciaId; if (!id) return;
    this.enviando.set(true); this.erro.set(null);
    this.api.arquivar(id, this.justificativa).subscribe({
      next: () => { this.enviando.set(false); this.selecionado.set(null); this.mensagem.set('Denúncia arquivada como improcedente.'); this.atualizar(); },
      error: (e: ErroApi) => { this.enviando.set(false); this.erro.set(e.mensagem); },
    });
  }

  protected advertir(): void {
    const usuarioId = this.previa()?.autorId; if (!usuarioId || !this.motivoAdvertencia.trim()) return;
    this.enviando.set(true); this.erro.set(null);
    this.api.advertir(usuarioId, this.motivoAdvertencia).subscribe({
      next: () => { this.enviando.set(false); this.motivoAdvertencia = ''; this.avisos.update((n) => (n ?? 0) + 1); this.mensagem.set('Advertência registrada e autor notificado.'); this.atualizar(); },
      error: (e: ErroApi) => { this.enviando.set(false); this.erro.set(e.mensagem); },
    });
  }

  protected mudarPaginaFila(delta: number): void { this.paginaFila.update((n) => Math.max(1, n + delta)); this.atualizar(); }
  protected mudarPaginaLogs(delta: number): void { this.paginaLogs.update((n) => Math.max(1, n + delta)); this.atualizar(); }
}
