import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

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

  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly sucesso = signal<string | null>(null);
  protected readonly formulario = inject(FormBuilder).nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    senha: ['', [Validators.required, Validators.minLength(8)]],
    papel: ['DESENVOLVEDOR' as Papel, [Validators.required]],
  });

  cadastrar(): void {
    if (this.formulario.invalid || this.carregando()) return;
    this.carregando.set(true);
    this.erro.set(null);
    this.sucesso.set(null);
    this.autenticacao.registrarPrivilegiado(this.formulario.getRawValue()).subscribe({
      next: (u) => {
        this.sucesso.set(`Conta criada: ${u.email} (${u.papel})`);
        this.formulario.reset({ nome: '', email: '', senha: '', papel: 'DESENVOLVEDOR' });
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }
}