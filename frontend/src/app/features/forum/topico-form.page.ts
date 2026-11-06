import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { ForumService } from '../../core/api/forum.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

/** Cria um tópico (rota forum/novo) ou edita um existente (rota forum/:id/editar). */
@Component({
  selector: 'app-topico-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './topico-form.page.html',
  styleUrl: './topico-form.page.scss',
})
export class TopicoFormPage {
  private readonly forum = inject(ForumService);
  private readonly catalogo = inject(CatalogoService);
  private readonly roteador = inject(Router);
  private readonly builder = inject(FormBuilder).nonNullable;
  protected readonly autenticacao = inject(AutenticacaoService);
  protected readonly id = inject(ActivatedRoute).snapshot.paramMap.get('id');

  protected readonly jogos = signal<Jogo[]>([]);
  protected readonly carregando = signal(true);
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly formulario = this.builder.group({
    jogoId: [''],
    categoria: ['', Validators.maxLength(60)],
    titulo: ['', [Validators.required, Validators.maxLength(150)]],
    conteudo: ['', [Validators.required, Validators.maxLength(10000)]],
  });

  constructor() {
    if (!this.autenticacao.usuarioAtual()) {
      this.carregando.set(false);
      return;
    }
    this.catalogo.listarJogos().subscribe({ next: (jogos) => this.jogos.set(jogos) });
    if (!this.id) {
      this.carregando.set(false);
      return;
    }
    this.formulario.controls.jogoId.disable();
    this.forum.buscarTopico(this.id).subscribe({
      next: (topico) => {
        this.formulario.setValue({
          jogoId: topico.jogoId ?? '',
          categoria: topico.categoria,
          titulo: topico.titulo,
          conteudo: topico.conteudo,
        });
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }

  protected salvar(): void {
    if (this.formulario.invalid || this.salvando()) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    this.erro.set(null);
    const dados = this.formulario.getRawValue();
    const corpo = { ...dados, jogoId: dados.jogoId || null };
    const requisicao = this.id ? this.forum.editar(this.id, corpo) : this.forum.criar(corpo);
    requisicao.subscribe({
      next: (topico) => this.roteador.navigate(['/forum', topico.id]),
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.salvando.set(false);
      },
    });
  }
}
