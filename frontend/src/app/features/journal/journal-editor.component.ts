import { Component, DestroyRef, ElementRef, computed, inject, output, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { AnexoJournal, EntradaJournal, JournalService } from '../../core/api/journal.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-journal-editor',
  imports: [ReactiveFormsModule],
  templateUrl: './journal-editor.component.html',
  styleUrl: './journal-editor.component.scss',
})
export class JournalEditorComponent {
  private readonly api = inject(JournalService);
  private readonly catalogo = inject(CatalogoService);
  private readonly builder = inject(FormBuilder).nonNullable;
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');
  readonly salvo = output<void>();

  protected readonly entrada = signal<EntradaJournal | null>(null);
  protected readonly jogos = signal<Jogo[]>([]);
  protected readonly jogoAusente = computed(() => !!this.entrada()?.jogoId
    && !this.jogos().some((jogo) => jogo.id === this.entrada()?.jogoId));
  protected readonly carregandoJogos = signal(false);
  protected readonly erroJogos = signal<string | null>(null);
  protected readonly mantidos = signal<AnexoJournal[]>([]);
  protected readonly novas = signal<{ arquivo: File; url: string }[]>([]);
  protected readonly totalImagens = computed(() => this.mantidos().length + this.novas().length);
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly tags = signal<string[]>([]);
  protected readonly formulario = this.builder.group({
    jogoId: [''], dataSessao: ['', Validators.required], titulo: ['', Validators.maxLength(120)],
    texto: ['', [Validators.required, Validators.maxLength(5000)]], tags: [''], contemSpoiler: [false],
  });

  constructor() {
    this.formulario.controls.tags.valueChanges.pipe(takeUntilDestroyed()).subscribe((valor) => {
      this.tags.set([...new Set(valor.split(',').map((tag) => tag.trim().replace(/^#/, '').trim().toLowerCase()).filter(Boolean))]);
    });
    this.destroyRef.onDestroy(() => this.limparImagens());
  }

  abrir(entrada?: EntradaJournal): void {
    if (entrada && !entrada.editavel) return;
    this.limparImagens();
    this.entrada.set(entrada ?? null);
    this.mantidos.set(entrada?.anexos ?? []);
    this.erro.set(null);
    const hoje = new Date();
    const data = `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, '0')}-${String(hoje.getDate()).padStart(2, '0')}`;
    this.formulario.reset({ jogoId: entrada?.jogoId ?? '', dataSessao: entrada?.dataSessao.slice(0, 10) ?? data,
      titulo: entrada?.titulo ?? '', texto: entrada?.texto ?? '', tags: entrada?.tags.join(', ') ?? '',
      contemSpoiler: entrada?.contemSpoiler ?? false });
    this.dialogo().nativeElement.showModal();
    this.carregarJogos();
  }

  protected carregarJogos(): void {
    if (this.carregandoJogos()) return;
    this.carregandoJogos.set(true);
    this.erroJogos.set(null);
    this.catalogo.listarJogos().pipe(takeUntilDestroyed(this.destroyRef),
      finalize(() => this.carregandoJogos.set(false))).subscribe({
      next: (jogos) => this.jogos.set(jogos),
      error: (erro: ErroApi) => this.erroJogos.set(erro.mensagem),
    });
  }

  protected escolher(evento: Event): void {
    const entrada = evento.target as HTMLInputElement;
    const arquivos = Array.from(entrada.files ?? []);
    entrada.value = '';
    if (!arquivos.length || this.salvando()) return;
    this.erro.set(null);
    if (this.totalImagens() + arquivos.length > 3) {
      this.erro.set('Cada entrada aceita no máximo 3 imagens'); return;
    }
    if (arquivos.some((arquivo) => !['image/png', 'image/jpeg', 'image/webp'].includes(arquivo.type))) {
      this.erro.set('Envie uma imagem PNG, JPG ou WebP válida'); return;
    }
    if (arquivos.some((arquivo) => arquivo.size === 0 || arquivo.size > 2 * 1024 * 1024)) {
      this.erro.set('Cada imagem deve ter conteúdo e no máximo 2 MB'); return;
    }
    this.novas.update((atuais) => [...atuais, ...arquivos.map((arquivo) => ({ arquivo, url: URL.createObjectURL(arquivo) }))]);
  }

  protected removerNova(indice: number): void {
    URL.revokeObjectURL(this.novas()[indice].url);
    this.novas.update((imagens) => imagens.filter((_, i) => i !== indice));
  }

  protected removerMantida(id: string): void {
    this.mantidos.update((imagens) => imagens.filter((imagem) => imagem.id !== id));
  }

  protected imagem(anexo: AnexoJournal): string {
    return this.api.imagemUrl(this.entrada()!.id, anexo.id, true);
  }

  protected salvar(): void {
    if (this.salvando()) return;
    this.formulario.markAllAsTouched();
    if (this.formulario.invalid || this.tags().length > 6) return;
    const campos = this.formulario.getRawValue();
    if (!campos.texto.trim()) { this.erro.set('O texto da entrada é obrigatório'); return; }
    this.salvando.set(true);
    this.erro.set(null);
    this.api.salvar({ ...campos, jogoId: campos.jogoId || null, tags: this.tags(),
      anexosMantidos: this.mantidos().map((imagem) => imagem.id) }, this.novas().map((imagem) => imagem.arquivo),
      this.entrada()?.id).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.salvando.set(false))).subscribe({
      next: () => { this.dialogo().nativeElement.close(); this.salvo.emit(); },
      error: (erro: ErroApi) => this.erro.set(erro.mensagem),
    });
  }

  protected fechar(): void { if (!this.salvando()) this.dialogo().nativeElement.close(); }
  protected limparImagens(): void {
    for (const imagem of this.novas()) URL.revokeObjectURL(imagem.url);
    this.novas.set([]);
  }
}
