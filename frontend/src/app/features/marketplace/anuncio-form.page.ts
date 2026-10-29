import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import {
  ESTADOS,
  EstadoConservacao,
  MIDIAS,
  MarketplaceService,
  MidiaJogo,
  TIPOS_NEGOCIACAO,
  TipoNegociacao,
} from '../../core/api/marketplace.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

/** Cria um anúncio (rota marketplace/novo) ou edita um existente (rota marketplace/:id/editar). */
@Component({
  selector: 'app-anuncio-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './anuncio-form.page.html',
  styleUrl: './anuncio-form.page.scss',
})
export class AnuncioFormPage {
  private readonly marketplace = inject(MarketplaceService);
  private readonly catalogo = inject(CatalogoService);
  private readonly roteador = inject(Router);
  private readonly builder = inject(FormBuilder).nonNullable;
  protected readonly autenticacao = inject(AutenticacaoService);
  protected readonly id = inject(ActivatedRoute).snapshot.paramMap.get('id');

  protected readonly estados = Object.entries(ESTADOS) as [EstadoConservacao, string][];
  protected readonly tipos = Object.entries(TIPOS_NEGOCIACAO) as [TipoNegociacao, string][];
  protected readonly midias = Object.entries(MIDIAS) as [MidiaJogo, string][];
  protected readonly jogos = signal<Jogo[]>([]);
  protected readonly fotos = signal<string[]>([]);
  protected readonly carregando = signal(true);
  protected readonly salvando = signal(false);
  protected readonly enviandoFotos = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly formulario = this.builder.group({
    jogoId: ['', Validators.required],
    titulo: ['', Validators.maxLength(180)],
    estado: this.builder.control<EstadoConservacao>('USADO', Validators.required),
    preco: this.builder.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    tipoNegociacao: this.builder.control<TipoNegociacao>('VENDA', Validators.required),
    midia: this.builder.control<MidiaJogo>('FISICO', Validators.required),
    localidade: ['', [Validators.required, Validators.maxLength(120)]],
  });

  constructor() {
    if (!this.autenticacao.usuarioAtual()) {
      this.carregando.set(false);
      return;
    }
    if (this.id) {
      this.formulario.controls.jogoId.disable();
      forkJoin({ jogos: this.catalogo.listarJogos(), anuncio: this.marketplace.buscarAnuncio(this.id) }).subscribe({
        next: ({ jogos, anuncio }) => {
          this.jogos.set(jogos);
          this.formulario.setValue({
            jogoId: anuncio.jogoId,
            titulo: anuncio.titulo,
            estado: anuncio.estado,
            preco: anuncio.preco,
            tipoNegociacao: anuncio.tipoNegociacao,
            midia: anuncio.midia,
            localidade: anuncio.localidade,
          });
          this.fotos.set(anuncio.fotos);
          this.carregando.set(false);
        },
        error: (e: ErroApi) => this.falhar(e),
      });
    } else {
      this.catalogo.listarJogos().subscribe({
        next: (jogos) => {
          this.jogos.set(jogos);
          this.carregando.set(false);
        },
        error: (e: ErroApi) => this.falhar(e),
      });
    }
  }

  protected imagemUrl(caminho: string): string {
    return caminho.startsWith('/uploads/') || /^https?:\/\//.test(caminho) ? caminho : '';
  }

  protected selecionarFotos(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const imagens = Array.from(input.files ?? []);
    if (!imagens.length) return;

    this.erro.set(null);
    this.enviandoFotos.set(true);
    forkJoin(imagens.map((imagem) => this.marketplace.enviarFoto(imagem))).subscribe({
      next: (respostas) => {
        this.fotos.update((atuais) => [...atuais, ...respostas.map((resposta) => resposta.caminho)]);
        this.enviandoFotos.set(false);
        input.value = '';
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.enviandoFotos.set(false);
        input.value = '';
      },
    });
  }

  protected removerFoto(indice: number): void {
    this.fotos.update((atuais) => atuais.filter((_, i) => i !== indice));
  }

  protected salvar(): void {
    if (this.formulario.invalid || this.salvando() || this.enviandoFotos()) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    this.erro.set(null);
    const dados = this.formulario.getRawValue();
    const corpo = { ...dados, preco: Number(dados.preco), fotos: this.fotos() };
    const requisicao = this.id ? this.marketplace.editar(this.id, corpo) : this.marketplace.criar(corpo);
    requisicao.subscribe({
      next: () => this.roteador.navigate(['/marketplace/meus']),
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.salvando.set(false);
      },
    });
  }

  private falhar(e: ErroApi): void {
    this.erro.set(e.mensagem);
    this.carregando.set(false);
  }
}
