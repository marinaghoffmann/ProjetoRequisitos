import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin, map } from 'rxjs';

import {
  CatalogoService,
  Desenvolvedora,
  Genero,
} from '../../core/api/catalogo.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-cadastro-jogo',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cadastro-jogo.page.html',
  styleUrl: './cadastro-jogo.page.scss',
})
export class CadastroJogoPage {
  private readonly formularioBuilder = inject(FormBuilder).nonNullable;
  private readonly catalogo = inject(CatalogoService);
  protected readonly autenticacao = inject(AutenticacaoService);

  protected readonly desenvolvedoras = signal<Desenvolvedora[]>([]);
  protected readonly generos = signal<Genero[]>([]);
  protected readonly carregando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly importandoSteam = signal(false);
  protected readonly enviandoImagem = signal(false);
  protected readonly enviandoGaleria = signal(false);
  protected readonly nomeImagem = signal<string | null>(null);
  protected readonly nomesGaleria = signal<string[]>([]);
  protected readonly erro = signal<string | null>(null);
  protected readonly sucesso = signal<string | null>(null);
  protected readonly desenvolvedoraVinculada = computed(() => {
    const id = this.autenticacao.usuarioAtual()?.desenvolvedoraId;
    return this.desenvolvedoras().find((item) => item.id === id) ?? null;
  });

  protected readonly formulario = this.formularioBuilder.group({
    titulo: ['', [Validators.required, Validators.maxLength(180)]],
    tituloOriginal: ['', [Validators.maxLength(180)]],
    resumo: ['', [Validators.maxLength(600)]],
    generosIds: this.formularioBuilder.control<string[]>([]),
    dataLancamento: [''],
    capaUrl: ['', [Validators.maxLength(1000)]],
    trailerUrl: ['', [Validators.maxLength(1000)]],
    generoPrincipal: [''],
    classificacaoIndicativa: [''],
    subgeneros: [''],
    temas: [''],
    avisosConteudo: [''],
    plataformas: [''],
    modosJogo: [''],
    metodosEntrada: [''],
    statusLancamento: [''],
    edicao: [''],
    precoSugerido: [''],
    disponibilidade: [''],
    publicadora: [''],
    engine: [''],
    franquia: [''],
    siteOficial: ['', [Validators.maxLength(1000)]],
    galerias: [''],
    modeloComercial: [''],
    infraestruturaOnline: [''],
    crossplay: [false],
    salvamentoNuvem: [false],
    informacoesOficiais: [false],
    idiomasInterface: [''],
    idiomasAudio: [''],
    acessibilidade: [''],
  });
  protected readonly termoSteam = this.formularioBuilder.control('', [Validators.required, Validators.maxLength(160)]);

  constructor() {
    forkJoin({
      desenvolvedoras: this.catalogo.listarDesenvolvedoras(),
      generos: this.catalogo.listarGeneros(),
    }).subscribe({
      next: (dados) => {
        this.desenvolvedoras.set(dados.desenvolvedoras.filter((item) => item.ativa));
        this.generos.set(dados.generos);
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }

  protected alternarGenero(id: string, evento: Event): void {
    const marcado = (evento.target as HTMLInputElement).checked;
    const selecionados = this.formulario.controls.generosIds.value;
    this.formulario.controls.generosIds.setValue(
      marcado ? [...selecionados, id] : selecionados.filter((item) => item !== id),
    );
  }

  protected selecionarCapa(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const imagem = input.files?.[0];
    if (!imagem) return;

    this.erro.set(null);
    this.sucesso.set(null);
    this.enviandoImagem.set(true);
    this.catalogo.enviarCapa(imagem).subscribe({
      next: (resposta) => {
        this.formulario.controls.capaUrl.setValue(resposta.caminho);
        this.nomeImagem.set(imagem.name);
        this.enviandoImagem.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.enviandoImagem.set(false);
        input.value = '';
      },
    });
  }

  protected selecionarGaleria(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const imagens = Array.from(input.files ?? []);
    if (!imagens.length) return;

    this.erro.set(null);
    this.enviandoGaleria.set(true);
    this.catalogo.enviarGaleria(imagens).subscribe({
      next: (caminhos) => {
        this.formulario.controls.galerias.setValue(caminhos.join(','));
        this.nomesGaleria.set(imagens.map((imagem) => imagem.name));
        this.enviandoGaleria.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.enviandoGaleria.set(false);
        input.value = '';
      },
    });
  }

  protected cadastrar(): void {
    if (this.formulario.invalid || this.enviando() || this.enviandoImagem() || this.enviandoGaleria()) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.erro.set(null);
    this.sucesso.set(null);
    const dados = this.formulario.getRawValue();

    this.catalogo.cadastrarJogo({
      titulo: dados.titulo,
      tituloOriginal: dados.tituloOriginal,
      resumo: dados.resumo,
      desenvolvedoraId: null,
      generosIds: dados.generosIds,
      dataLancamento: dados.dataLancamento || null,
      capaUrl: dados.capaUrl,
      trailerUrl: dados.trailerUrl,
      metadados: {
        generoPrincipal: dados.generoPrincipal,
        classificacaoIndicativa: dados.classificacaoIndicativa,
        subgeneros: this.lista(dados.subgeneros),
        temas: this.lista(dados.temas),
        avisosConteudo: this.lista(dados.avisosConteudo),
        plataformas: this.lista(dados.plataformas),
        modosJogo: this.lista(dados.modosJogo),
        metodosEntrada: this.lista(dados.metodosEntrada),
        statusLancamento: dados.statusLancamento,
        edicao: dados.edicao,
        precoSugerido: dados.precoSugerido,
        disponibilidade: dados.disponibilidade,
        publicadora: dados.publicadora,
        engine: dados.engine,
        franquia: dados.franquia,
        siteOficial: dados.siteOficial,
        galerias: this.lista(dados.galerias),
        modeloComercial: dados.modeloComercial,
        infraestruturaOnline: dados.infraestruturaOnline,
        crossplay: dados.crossplay,
        salvamentoNuvem: dados.salvamentoNuvem,
        informacoesOficiais: dados.informacoesOficiais,
        idiomasInterface: this.lista(dados.idiomasInterface),
        idiomasAudio: this.lista(dados.idiomasAudio),
        acessibilidade: this.lista(dados.acessibilidade),
      },
    }).subscribe({
      next: (jogo) => {
        this.enviando.set(false);
        this.sucesso.set(`"${jogo.titulo}" foi cadastrado com sucesso.`);
        this.formulario.reset({
          titulo: '',
          tituloOriginal: '',
          resumo: '',
          generosIds: [],
          dataLancamento: '',
          capaUrl: '',
          trailerUrl: '',
          generoPrincipal: '',
          classificacaoIndicativa: '',
          subgeneros: '',
          temas: '',
          avisosConteudo: '',
          plataformas: '',
          modosJogo: '',
          metodosEntrada: '',
          statusLancamento: '',
          edicao: '',
          precoSugerido: '',
          disponibilidade: '',
          publicadora: '',
          engine: '',
          franquia: '',
          siteOficial: '',
          galerias: '',
          modeloComercial: '',
          infraestruturaOnline: '',
          crossplay: false,
          salvamentoNuvem: false,
          informacoesOficiais: false,
          idiomasInterface: '',
          idiomasAudio: '',
          acessibilidade: '',
        });
        this.nomeImagem.set(null);
        this.nomesGaleria.set([]);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.enviando.set(false);
      },
    });
  }

  protected importarDaSteam(): void {
    if (this.termoSteam.invalid || this.importandoSteam()) {
      this.termoSteam.markAsTouched();
      return;
    }

    this.importandoSteam.set(true);
    this.erro.set(null);
    this.sucesso.set(null);
    this.catalogo.importarSteam(this.termoSteam.getRawValue().trim()).subscribe({
      next: (jogo) => {
        this.importandoSteam.set(false);
        this.termoSteam.reset();
        this.sucesso.set(`"${jogo.titulo}" foi importado da Steam e cadastrado com sucesso.`);
      },
      error: (e: ErroApi) => {
        this.importandoSteam.set(false);
        this.erro.set(e.mensagem);
      },
    });
  }

  private lista(valor: string): string[] {
    return valor.split(',').map((item) => item.trim()).filter(Boolean);
  }
}
