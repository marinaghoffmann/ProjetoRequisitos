import { Component, DestroyRef, ElementRef, OnInit, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';

import { ConclusoesService, RegistroDeConclusao } from '../../core/api/conclusoes.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-conclusoes-jogo',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './conclusoes-jogo.component.html',
  styleUrl: './conclusoes-jogo.component.scss',
})
export class ConclusoesJogoComponent implements OnInit {
  private readonly api = inject(ConclusoesService);
  private readonly host: ElementRef<HTMLElement> = inject(ElementRef);
  private readonly destroyRef = inject(DestroyRef);

  readonly jogoId = input.required<string>();
  readonly alterado = output<void>();

  protected readonly registros = signal<RegistroDeConclusao[] | null>(null);
  protected readonly formularioAberto = signal(false);
  protected readonly editando = signal<string | null>(null);
  protected readonly excluindo = signal<string | null>(null);
  protected readonly carregando = signal(false);
  protected readonly salvando = signal(false);
  protected readonly erroHistorico = signal<string | null>(null);
  protected readonly erroAcao = signal<string | null>(null);
  protected readonly formulario = new FormGroup({
    dataConclusao: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    plataforma: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(100)] }),
    horasJogadas: new FormControl<number | null>(null),
    concluido: new FormControl(true, { nonNullable: true }),
    platina: new FormControl(false, { nonNullable: true }),
  });

  ngOnInit(): void {
    this.carregar();
  }

  nova(): void {
    this.host.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    if (this.salvando()) return;

    const agora = new Date();
    const hoje = new Date(agora.getTime() - agora.getTimezoneOffset() * 60_000)
      .toISOString().slice(0, 10);
    this.abrirFormulario();
    this.formulario.reset({ dataConclusao: hoje });
  }

  protected editar(registro: RegistroDeConclusao): void {
    this.abrirFormulario(registro.id);
    this.formulario.reset({ ...registro, plataforma: registro.plataforma ?? '' });
    this.host.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  protected salvar(): void {
    if (this.formulario.invalid || this.salvando() || this.carregando()) return;

    const dados = this.formulario.getRawValue();
    const id = this.editando();
    const requisicao = id ? this.api.editar(id, dados) : this.api.registrar(this.jogoId(), dados);
    this.salvando.set(true);
    this.erroAcao.set(null);
    requisicao.pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.salvando.set(false)),
    ).subscribe({
      next: () => {
        this.formularioAberto.set(false);
        this.carregar();
        this.alterado.emit();
      },
      error: (erro: ErroApi) => this.erroAcao.set(erro.mensagem),
    });
  }

  protected confirmarExclusao(): void {
    const id = this.excluindo();
    if (!id || this.salvando()) return;

    this.salvando.set(true);
    this.erroAcao.set(null);
    this.api.excluir(id).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.salvando.set(false)),
    ).subscribe({
      next: () => {
        this.excluindo.set(null);
        if (this.editando() === id) this.formularioAberto.set(false);
        this.carregar();
        this.alterado.emit();
      },
      error: (erro: ErroApi) => this.erroAcao.set(erro.mensagem),
    });
  }

  protected carregar(): void {
    if (this.carregando()) return;

    this.carregando.set(true);
    this.erroHistorico.set(null);
    this.api.historico(this.jogoId()).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.carregando.set(false)),
    ).subscribe({
      next: (registros) => this.registros.set(registros),
      error: (erro: ErroApi) => this.erroHistorico.set(erro.mensagem),
    });
  }

  private abrirFormulario(id: string | null = null): void {
    this.editando.set(id);
    this.excluindo.set(null);
    this.erroAcao.set(null);
    this.formularioAberto.set(true);
  }
}
