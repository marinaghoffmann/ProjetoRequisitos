import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-cadastro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cadastro.page.html',
  styleUrl: './identidade.page.scss',
})
export class CadastroPage {
  private readonly formularioBuilder = inject(FormBuilder).nonNullable;
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly router = inject(Router);

  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly formulario = this.formularioBuilder.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    senha: ['', [Validators.required, Validators.minLength(8)]],
  });

  cadastrar(): void {
    if (this.formulario.invalid || this.carregando()) return;
    this.carregando.set(true);
    this.erro.set(null);
    this.autenticacao.registrar(this.formulario.getRawValue()).subscribe({
      next: () => void this.router.navigateByUrl('/login?cadastro=feito'),
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
      complete: () => this.carregando.set(false),
    });
  }
}
