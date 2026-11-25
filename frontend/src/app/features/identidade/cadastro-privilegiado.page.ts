import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { CatalogoService, Desenvolvedora } from '../../core/api/catalogo.service';
import { AutenticacaoService, Papel } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-cadastro-privilegiado',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cadastro-privilegiado.page.html',
  styleUrl: './identidade.page.scss',
})
export class CadastroPrivilegiadoPage {
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly catalogo = inject(CatalogoService);

  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly sucesso = signal<string | null>(null);
  protected readonly desenvolvedoras = signal<Desenvolvedora[]>([]);
  protected readonly formulario = inject(FormBuilder).nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    senha: ['', [Validators.required, Validators.minLength(8)]],
    papel: ['DESENVOLVEDOR' as Papel, [Validators.required]],
    desenvolvedoraId: ['', [Validators.required]],
  });

  constructor() {
    const destroyRef = inject(DestroyRef);
    this.catalogo.listarDesenvolvedoras().subscribe({
      next: (lista) => this.desenvolvedoras.set(lista.filter((d) => d.ativa)),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
    // A desenvolvedora só é obrigatória quando o papel é Desenvolvedor.
    this.formulario.controls.papel.valueChanges.pipe(takeUntilDestroyed(destroyRef)).subscribe((papel) => {
      const campo = this.formulario.controls.desenvolvedoraId;
      if (papel === 'DESENVOLVEDOR') campo.addValidators(Validators.required);
      else campo.clearValidators();
      campo.updateValueAndValidity();
    });
  }

  protected ehDesenvolvedor(): boolean {
    return this.formulario.controls.papel.value === 'DESENVOLVEDOR';
  }

  cadastrar(): void {
    if (this.formulario.invalid || this.carregando()) return;
    this.carregando.set(true);
    this.erro.set(null);
    this.sucesso.set(null);

    const { desenvolvedoraId, ...dados } = this.formulario.getRawValue();
    const vincular = dados.papel === 'DESENVOLVEDOR';

    this.autenticacao.registrarPrivilegiado(dados).subscribe({
      next: (u) => {
        if (!vincular) return this.concluir(`Conta criada: ${u.email} (${u.papel})`);
        this.autenticacao.vincularDesenvolvedora(u.id, desenvolvedoraId).subscribe({
          next: () => {
            const nome = this.desenvolvedoras().find((d) => d.id === desenvolvedoraId)?.nome ?? '';
            this.concluir(`Conta criada: ${u.email} (${u.papel}), vinculada a ${nome}`);
          },
          error: (e: ErroApi) => {
            this.carregando.set(false);
            this.erro.set(`A conta ${u.email} foi criada, mas o vínculo falhou: ${e.mensagem}`);
          },
        });
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }

  private concluir(mensagem: string): void {
    this.sucesso.set(mensagem);
    this.formulario.reset({ nome: '', email: '', senha: '', papel: 'DESENVOLVEDOR', desenvolvedoraId: '' });
    this.carregando.set(false);
  }
}