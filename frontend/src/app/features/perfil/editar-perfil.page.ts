import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import {
  AutenticacaoService, TipoImagemPerfil, UsuarioAutenticado,
} from '../../core/auth/autenticacao.service';
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

  protected readonly usuario = this.autenticacao.usuarioAtual;
  protected readonly enviando = signal<TipoImagemPerfil | null>(null);
  private readonly tiposAceitos = ['image/jpeg', 'image/png', 'image/webp'];
  private readonly tamanhoMaximo = 5 * 1024 * 1024;

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

  /** Foto e banner são enviados na hora, separados do botão "Salvar alterações". */
  protected escolher(tipo: TipoImagemPerfil, evento: Event): void {
    const entrada = evento.target as HTMLInputElement;
    const arquivo = entrada.files?.[0];
    entrada.value = ''; // permite escolher o mesmo arquivo de novo
    if (!arquivo || this.enviando()) return;

    if (!this.tiposAceitos.includes(arquivo.type)) {
      this.erro.set('Use uma imagem JPG, PNG ou WebP.');
      return;
    }
    if (arquivo.size > this.tamanhoMaximo) {
      this.erro.set('A imagem deve ter no máximo 5 MB.');
      return;
    }

    this.erro.set(null);
    this.enviando.set(tipo);
    this.autenticacao.enviarImagem(tipo, arquivo).subscribe({
      next: () => this.enviando.set(null),
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.enviando.set(null);
      },
    });
  }

  protected imagem(url: string | null | undefined): string | null {
    return url ? `url("${encodeURI(url)}") center / cover no-repeat` : null;
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