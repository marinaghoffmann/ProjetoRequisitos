import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AutenticacaoService, UsuarioAutenticado } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

/** Edita nome, bio e localização do usuário logado (rota perfil/editar). */
@Component({
  selector: 'app-editar-perfil',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './editar-perfil.page.html',
  styleUrl: './editar-perfil.page.scss',
})
export class EditarPerfilPage {
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly roteador = inject(Router);
  private readonly builder = inject(FormBuilder).nonNullable;

  protected readonly limiteBio = 160;
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly formulario = this.builder.group({
    nome: ['', [Validators.required, Validators.maxLength(80)]],
    bio: ['', Validators.maxLength(this.limiteBio)],
    localizacao: ['', Validators.maxLength(80)],
  });

  constructor() {
    this.preencher(this.autenticacao.usuarioAtual());
    // A sessão pode ter sido salva antes de existirem bio e localização: busca os dados atuais.
    this.autenticacao.recarregarUsuario().subscribe({
      next: (usuario) => {
        if (!this.formulario.dirty) this.preencher(usuario);
      },
      error: () => {},
    });
  }

  protected salvar(): void {
    if (this.formulario.invalid || this.salvando()) {
      this.formulario.markAllAsTouched();
      return;
    }
    const dados = this.formulario.getRawValue();
    const nome = dados.nome.trim();
    if (!nome) {
      this.erro.set('O nome é obrigatório.');
      return;
    }

    this.salvando.set(true);
    this.erro.set(null);
    this.autenticacao
      .atualizarPerfil({ nome, bio: dados.bio.trim(), localizacao: dados.localizacao.trim() })
      .subscribe({
        next: () => this.roteador.navigate(['/perfil']),
        error: (e: ErroApi) => {
          this.erro.set(e.mensagem);
          this.salvando.set(false);
        },
      });
  }

  private preencher(usuario: UsuarioAutenticado | null): void {
    if (!usuario) return;
    this.formulario.patchValue({
      nome: usuario.nome,
      bio: usuario.bio ?? '',
      localizacao: usuario.localizacao ?? '',
    });
  }
}