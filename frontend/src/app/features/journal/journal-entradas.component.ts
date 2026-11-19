import { DatePipe } from '@angular/common';
import { Component, DestroyRef, effect, inject, input, signal, untracked, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize, Subscription } from 'rxjs';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { Pagina } from '../../core/api/forum.service';
import { EntradaJournal, JournalService } from '../../core/api/journal.service';
import { ErroApi } from '../../core/http/erro-api';
import { ConteudoModeravelComponent } from '../moderacao/conteudo-moderavel.component';
import { JournalEditorComponent } from './journal-editor.component';

@Component({
  selector: 'app-journal-entradas',
  imports: [DatePipe, ReactiveFormsModule, RouterLink, JournalEditorComponent, ConteudoModeravelComponent],
  templateUrl: './journal-entradas.component.html',
  styleUrl: './journal-entradas.component.scss',
})
export class JournalEntradasComponent {
  private readonly api = inject(JournalService);
  private readonly catalogo = inject(CatalogoService);
  private readonly builder = inject(FormBuilder).nonNullable;
  private readonly destroyRef = inject(DestroyRef);
  private consulta?: Subscription;
  readonly publico = input(false);
  readonly donoId = input<string | null>(null);
  protected readonly editor = viewChild(JournalEditorComponent);
  protected readonly jogos = signal<Jogo[]>([]);
  protected readonly tags = signal<string[]>([]);
  protected readonly pagina = signal<Pagina<EntradaJournal> | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroCatalogo = signal<string | null>(null);
  protected readonly erroTags = signal<string | null>(null);
  protected readonly mensagem = signal('');
  protected readonly ocupada = signal<string | null>(null);
  protected readonly reveladas = signal(new Map<string, EntradaJournal>());
  protected readonly filtros = this.builder.group({ jogoId: [''], tag: [''], inicio: [''], fim: [''], origem: [''] });

  constructor() {
    effect(() => {
      this.publico(); this.donoId();
      untracked(() => { this.carregar(1); this.carregarTags(); });
    });
    this.carregarJogos();
  }

  protected carregar(numero: number): void {
    if (this.publico() && !this.donoId()) return;
    const filtros = this.filtros.getRawValue();
    if (filtros.inicio && filtros.fim && filtros.inicio > filtros.fim) {
      this.erro.set('A data inicial não pode ser posterior à data final'); return;
    }
    this.consulta?.unsubscribe();
    this.carregando.set(true);
    this.erro.set(null);
    this.reveladas.set(new Map());
    this.consulta = this.api.entradas(filtros, numero, this.publico() ? this.donoId() : null)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (pagina) => {
          if (numero > 1 && !pagina.itens.length) { this.carregar(numero - 1); return; }
          this.pagina.set(pagina);
          this.carregando.set(false);
        },
        error: (erro: ErroApi) => { this.erro.set(erro.mensagem); this.carregando.set(false); },
      });
  }

  protected carregarJogos(): void {
    this.erroCatalogo.set(null);
    this.catalogo.listarJogos().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (jogos) => this.jogos.set(jogos),
      error: (erro: ErroApi) => this.erroCatalogo.set(erro.mensagem),
    });
  }

  protected carregarTags(): void {
    if (this.publico() && !this.donoId()) return;
    this.erroTags.set(null);
    this.api.tags(this.publico() ? this.donoId() : null).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (tags) => this.tags.set(tags), error: (erro: ErroApi) => this.erroTags.set(erro.mensagem),
    });
  }

  protected limpar(): void { this.filtros.reset(); this.carregar(1); }

  protected salvo(): void {
    this.mensagem.set('Entrada salva com sucesso.'); this.carregar(1); this.carregarTags();
  }

  protected excluir(entrada: EntradaJournal): void {
    if (this.publico() || !entrada.editavel || this.ocupada()) return;
    this.ocupada.set(entrada.id); this.erro.set(null); this.mensagem.set('');
    this.api.excluir(entrada.id).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.ocupada.set(null))).subscribe({
      next: () => { this.mensagem.set('Entrada excluída.'); this.carregar(this.pagina()?.pagina ?? 1); this.carregarTags(); },
      error: (erro: ErroApi) => this.erro.set(erro.mensagem),
    });
  }

  protected revelar(entrada: EntradaJournal): void {
    const dono = this.donoId();
    if (!dono || this.ocupada()) return;
    this.ocupada.set(entrada.id); this.erro.set(null);
    this.api.revelar(dono, entrada.id).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.ocupada.set(null))).subscribe({
      next: (revelada) => this.reveladas.update((atuais) => new Map(atuais).set(entrada.id, revelada)),
      error: (erro: ErroApi) => this.erro.set(erro.mensagem),
    });
  }

  protected ocultar(id: string): void {
    this.reveladas.update((atuais) => { const novos = new Map(atuais); novos.delete(id); return novos; });
  }

  protected imagem(entrada: EntradaJournal, anexoId: string): string {
    return this.api.imagemUrl(entrada.id, anexoId, entrada.contemSpoiler && !entrada.mascarada);
  }
}
