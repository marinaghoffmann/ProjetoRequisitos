import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.page.html',
  styleUrl: './identidade.page.scss',
})
export class LoginPage {
  private readonly formularioBuilder = inject(FormBuilder).nonNullable;
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly router = inject(Router);

  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly formulario = this.formularioBuilder.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]],
  });

  entrar(): void {
    if (this.formulario.invalid || this.carregando()) return;
    this.carregando.set(true);
    this.erro.set(null);
    const { email, senha } = this.formulario.getRawValue();
    this.autenticacao.entrar(email, senha).subscribe({
      next: () => void this.router.navigateByUrl('/'),
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
      complete: () => this.carregando.set(false),
    });
  }
}
