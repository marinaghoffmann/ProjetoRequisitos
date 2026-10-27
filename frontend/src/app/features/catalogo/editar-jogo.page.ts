import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { CatalogoService, Desenvolvedora, Genero } from '../../core/api/catalogo.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-editar-jogo',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './editar-jogo.page.html',
  styleUrl: './editar-jogo.page.scss',
})
export class EditarJogoPage {
  private readonly catalogo = inject(CatalogoService);
  private readonly rota = inject(ActivatedRoute);
  private readonly builder = inject(FormBuilder).nonNullable;
  private readonly id = this.rota.snapshot.paramMap.get('id') ?? '';
  private metadados: Record<string, string | number | boolean | string[]> = {};

  protected readonly desenvolvedoras = signal<Desenvolvedora[]>([]);
  protected readonly generos = signal<Genero[]>([]);
  protected readonly carregando = signal(true);
  protected readonly salvando = signal(false);
  protected readonly enviandoImagem = signal(false);
  protected readonly enviandoGaleria = signal(false);
  protected readonly nomeImagem = signal<string | null>(null);
  protected readonly nomesGaleria = signal<string[]>([]);
  protected readonly erro = signal<string | null>(null);
  protected readonly sucesso = signal<string | null>(null);
  protected readonly formulario = this.builder.group({
    titulo: ['', [Validators.required, Validators.maxLength(180)]],
    tituloOriginal: ['', [Validators.maxLength(180)]],
    resumo: ['', [Validators.maxLength(600)]],
    desenvolvedoraId: ['', Validators.required],
    generosIds: this.builder.control<string[]>([]),
    dataLancamento: [''],
    capaUrl: ['', Validators.maxLength(1000)],
    trailerUrl: ['', Validators.maxLength(1000)],
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
    siteOficial: ['', Validators.maxLength(1000)],
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

  constructor() {
    forkJoin({
      jogo: this.catalogo.buscarJogo(this.id),
      desenvolvedoras: this.catalogo.listarDesenvolvedoras(),
      generos: this.catalogo.listarGeneros(),
    }).subscribe({
      next: ({ jogo, desenvolvedoras, generos }) => {
        if (jogo.metadadosJson) {
          try {
            this.metadados = JSON.parse(jogo.metadadosJson);
          } catch (erro) {
            this.erro.set(`Não foi possível ler os metadados do jogo: ${String(erro)}`);
          }
        }
        this.formulario.patchValue({
          titulo: jogo.titulo,
          tituloOriginal: jogo.tituloOriginal,
          resumo: jogo.resumo,
          desenvolvedoraId: jogo.desenvolvedoraId,
          generosIds: jogo.generosIds,
          dataLancamento: jogo.dataLancamento ?? '',
          capaUrl: jogo.capaUrl,
          trailerUrl: jogo.trailerUrl,
          generoPrincipal: this.valorTexto('generoPrincipal'),
          classificacaoIndicativa: this.valorTexto('classificacaoIndicativa'),
          subgeneros: this.valorLista('subgeneros'),
          temas: this.valorLista('temas'),
          avisosConteudo: this.valorLista('avisosConteudo'),
          plataformas: this.valorLista('plataformas'),
          modosJogo: this.valorLista('modosJogo'),
          metodosEntrada: this.valorLista('metodosEntrada'),
          statusLancamento: this.valorTexto('statusLancamento'),
          edicao: this.valorTexto('edicao'),
          precoSugerido: this.valorTexto('precoSugerido'),
          disponibilidade: this.valorTexto('disponibilidade'),
          publicadora: this.valorTexto('publicadora'),
          engine: this.valorTexto('engine'),
          franquia: this.valorTexto('franquia'),
          siteOficial: this.valorTexto('siteOficial'),
          galerias: this.valorLista('galerias'),
          modeloComercial: this.valorTexto('modeloComercial'),
          infraestruturaOnline: this.valorTexto('infraestruturaOnline'),
          crossplay: this.valorBooleano('crossplay'),
          salvamentoNuvem: this.valorBooleano('salvamentoNuvem'),
          informacoesOficiais: this.valorBooleano('informacoesOficiais'),
          idiomasInterface: this.valorLista('idiomasInterface'),
          idiomasAudio: this.valorLista('idiomasAudio'),
          acessibilidade: this.valorLista('acessibilidade'),
        });
        this.desenvolvedoras.set(desenvolvedoras.filter((item) => item.ativa));
        this.generos.set(generos);
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }

  protected alternarGenero(id: string, evento: Event): void {
    const ids = this.formulario.controls.generosIds.value;
    const marcado = (evento.target as HTMLInputElement).checked;
    this.formulario.controls.generosIds.setValue(
      marcado ? [...ids, id] : ids.filter((item) => item !== id),
    );
  }

  protected selecionarCapa(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const imagem = input.files?.[0];
    if (!imagem) return;
    this.enviandoImagem.set(true);
    this.erro.set(null);
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
    this.enviandoGaleria.set(true);
    this.erro.set(null);
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

  protected salvar(): void {
    if (this.formulario.invalid || this.salvando() || this.enviandoImagem() || this.enviandoGaleria()) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.salvando.set(true);
    this.erro.set(null);
    const dados = this.formulario.getRawValue();
    this.catalogo.atualizarJogo(this.id, {
      ...dados,
      dataLancamento: dados.dataLancamento || null,
      metadados: {
        ...this.metadados,
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
      next: () => {
        this.salvando.set(false);
        this.sucesso.set('Jogo atualizado com sucesso.');
      },
      error: (e: ErroApi) => {
        this.salvando.set(false);
        this.erro.set(e.mensagem);
      },
    });
  }

  private lista(valor: string): string[] {
    return valor.split(',').map((item) => item.trim()).filter(Boolean);
  }

  private valorTexto(chave: string): string {
    const valor = this.metadados[chave];
    return typeof valor === 'string' || typeof valor === 'number' ? String(valor) : '';
  }

  private valorLista(chave: string): string {
    const valor = this.metadados[chave];
    return Array.isArray(valor) ? valor.join(', ') : '';
  }

  private valorBooleano(chave: string): boolean {
    return this.metadados[chave] === true;
  }
}
