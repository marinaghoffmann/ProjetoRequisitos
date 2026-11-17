import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, DestroyRef, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { ConclusoesService, RegistroZeradoNaBiblioteca } from '../../core/api/conclusoes.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-zerados',
  imports: [DatePipe, DecimalPipe, ReactiveFormsModule, RouterLink],
  templateUrl: './zerados.page.html',
  styleUrl: './zerados.page.scss',
})
export class ZeradosPage {
  private readonly conclusoes = inject(ConclusoesService);
  private readonly catalogo = inject(CatalogoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');
  private readonly catalogoJogos = signal<Jogo[] | null>(null);

  protected readonly registros = signal<RegistroZeradoNaBiblioteca[] | null>(null);
  protected readonly filtro = signal('');
  protected readonly editando = signal<RegistroZeradoNaBiblioteca | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroForm = signal<string | null>(null);
  protected readonly erroCatalogo = signal<string | null>(null);
  protected readonly carregandoCatalogo = signal(false);
  protected readonly operando = signal(false);
  protected readonly feedback = signal('');
  protected readonly plataformas = ['Switch', 'PC', 'PS5', 'Xbox'];
  protected hoje = '';

  protected readonly formulario = new FormGroup({
    jogoId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    dataConclusao: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    plataforma: new FormControl('', { nonNullable: true }),
    horasJogadas: new FormControl<number | null>(null),
    concluido: new FormControl(true, { nonNullable: true }),
    platina: new FormControl(false, { nonNullable: true }),
  });

  protected readonly porJogo = computed(() => {
    const grupos = new Map<string, RegistroZeradoNaBiblioteca[]>();
    for (const registro of this.registros() ?? []) {
      const grupo = grupos.get(registro.jogoId);
      if (grupo) {
        grupo.push(registro);
      } else {
        grupos.set(registro.jogoId, [registro]);
      }
    }
    return grupos;
  });
  protected readonly jogosZerados = computed(() => [...this.porJogo().values()].map(([registro]) => registro));
  protected readonly visiveis = computed(() => {
    const jogoId = this.filtro();
    return jogoId ? this.porJogo().get(jogoId) ?? [] : this.jogosZerados();
  });
  protected readonly jogosDoFormulario = computed(() => {
    const jogos: Pick<Jogo, 'id' | 'titulo'>[] = [...(this.catalogoJogos() ?? [])];
    const atual = this.editando();
    if (atual && !jogos.some((jogo) => jogo.id === atual.jogoId)) {
      jogos.unshift({ id: atual.jogoId, titulo: atual.titulo });
    }
    return jogos;
  });

  constructor() {
    this.carregarRegistros();
  }

  protected carregarRegistros(): void {
    this.erro.set(null);
    this.registros.set(null);
    this.conclusoes.listarRegistros().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (registros) => {
        this.registros.set(registros);
        if (this.filtro() && !this.porJogo().has(this.filtro())) this.filtro.set('');
      },
      error: (erro: ErroApi) => this.erro.set(erro.mensagem),
    });
  }

  protected abrir(registro: RegistroZeradoNaBiblioteca | null = null): void {
    const agora = new Date();
    this.hoje = new Date(agora.getTime() - agora.getTimezoneOffset() * 60_000).toISOString().slice(0, 10);
    this.editando.set(registro);
    this.erroForm.set(null);
    this.formulario.reset({
      jogoId: registro?.jogoId ?? this.filtro(),
      dataConclusao: registro?.dataConclusao ?? this.hoje,
      plataforma: registro?.plataforma ?? '',
      horasJogadas: registro?.horasJogadas ?? null,
      concluido: registro?.concluido ?? true,
      platina: registro?.platina ?? false,
    });
    this.dialogo().nativeElement.showModal();
    this.carregarCatalogo();
  }

  protected fechar(): void {
    this.dialogo().nativeElement.close();
  }

  protected carregarCatalogo(): void {
    if (this.catalogoJogos() || this.carregandoCatalogo()) return;
    this.erroCatalogo.set(null);
    this.carregandoCatalogo.set(true);
    this.catalogo.listarJogos().pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.carregandoCatalogo.set(false)),
    ).subscribe({
      next: (jogos) => this.catalogoJogos.set(jogos),
      error: (erro: ErroApi) => this.erroCatalogo.set(erro.mensagem),
    });
  }

  protected salvar(): void {
    if (this.formulario.invalid || this.operando()) return;
    const { jogoId, ...dados } = this.formulario.getRawValue();
    const atual = this.editando();
    const requisicao = atual
      ? this.conclusoes.editar(atual.id, dados, jogoId)
      : this.conclusoes.registrar(jogoId, dados);

    this.feedback.set('');
    this.erroForm.set(null);
    this.operando.set(true);
    requisicao.pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.operando.set(false)),
    ).subscribe({
      next: () => {
        this.fechar();
        this.feedback.set(atual ? 'Registro atualizado.' : 'Registro criado.');
        this.carregarRegistros();
      },
      error: (erro: ErroApi) => this.erroForm.set(erro.mensagem),
    });
  }

  protected excluir(registro: RegistroZeradoNaBiblioteca): void {
    if (this.operando()) return;
    this.feedback.set('');
    this.erro.set(null);
    this.operando.set(true);
    this.conclusoes.excluir(registro.id).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.operando.set(false)),
    ).subscribe({
      next: () => {
        this.feedback.set('Registro excluído.');
        this.carregarRegistros();
      },
      error: (erro: ErroApi) => this.erro.set(erro.mensagem),
    });
  }
}
