import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { CatalogoService, Desenvolvedora, Genero } from '../../core/api/catalogo.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

const CHAVE_RASCUNHO = 'game-tracker:cadastro-jogo:rascunho';
const MAX_TAGS = 8;
const MAX_SCREENSHOTS = 10;

const minItens = (minimo: number): ValidatorFn => (c: AbstractControl): ValidationErrors | null =>
  ((c.value as unknown[])?.length ?? 0) >= minimo ? null : { minItens: true };

type CampoLista = 'plataformas' | 'modosJogo' | 'idiomas';

@Component({
  selector: 'app-cadastro-jogo',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cadastro-jogo.page.html',
  styleUrl: './cadastro-jogo.page.scss',
})
export class CadastroJogoPage {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly catalogo = inject(CatalogoService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly autenticacao = inject(AutenticacaoService);

  protected readonly plataformasOpcoes = ['PC', 'PS5', 'Xbox', 'Switch', 'Mobile'];
  protected readonly modosOpcoes = ['Single-player', 'Multiplayer local', 'Multiplayer online', 'Co-op', 'Competitivo'];
  protected readonly classificacoes = ['Livre', '10', '12', '14', '16', '18'];
  protected readonly idiomasOpcoes = ['Português (BR)', 'Inglês', 'Espanhol', 'Japonês', 'Francês', 'Alemão'];
  protected readonly maxTags = MAX_TAGS;
  protected readonly maxScreenshots = MAX_SCREENSHOTS;
  protected readonly nomesEtapas = ['Básico', 'Detalhes', 'Mídia', 'Revisão'];

  protected readonly desenvolvedoras = signal<Desenvolvedora[]>([]);
  protected readonly generos = signal<Genero[]>([]);
  protected readonly carregando = signal(true);
  protected readonly etapa = signal(1);
  protected readonly enviando = signal(false);
  protected readonly importandoSteam = signal(false);
  protected readonly enviandoImagem = signal<'capa' | 'banner' | 'screenshot' | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroEtapa = signal<string | null>(null);
  protected readonly sucesso = signal<string | null>(null);

  protected readonly formulario = this.fb.group({
    // Etapa 1
    titulo: ['', [Validators.required, Validators.maxLength(180)]],
    // Preenchido com a desenvolvedora vinculada; obrigatório para o desenvolvedor sem vínculo não avançar.
    desenvolvedoraId: ['', Validators.required],
    publicadora: ['', Validators.maxLength(120)],
    generoId: ['', Validators.required],
    dataLancamento: ['', Validators.required],
    plataformas: this.fb.control<string[]>([], minItens(1)),
    // Etapa 2
    modosJogo: this.fb.control<string[]>([], minItens(1)),
    classificacao: ['', Validators.required],
    idiomas: this.fb.control<string[]>([], minItens(1)),
    gratuito: [false],
    preco: [''],
    tags: this.fb.control<string[]>([]),
    resumo: ['', Validators.maxLength(600)],
    siteOficial: ['', Validators.maxLength(1000)],
    trailerUrl: ['', Validators.maxLength(1000)],
    // Etapa 3
    capaUrl: ['', Validators.maxLength(1000)],
    bannerUrl: ['', Validators.maxLength(1000)],
    screenshots: this.fb.control<string[]>([]),
  });

  protected readonly entradaTag = this.fb.control('');
  protected readonly entradaScreenshot = this.fb.control('');
  protected readonly termoSteam = this.fb.control('', [Validators.required, Validators.maxLength(160)]);

  private readonly camposDaEtapa: Record<number, string[]> = {
    1: ['titulo', 'desenvolvedoraId', 'publicadora', 'generoId', 'dataLancamento', 'plataformas'],
    2: ['modosJogo', 'classificacao', 'idiomas', 'preco', 'resumo', 'siteOficial', 'trailerUrl'],
    3: ['capaUrl', 'bannerUrl'],
    4: [],
  };

  constructor() {
    this.restaurarRascunho();
    this.formulario.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.salvarRascunho());

    forkJoin({
      desenvolvedoras: this.catalogo.listarDesenvolvedoras(),
      generos: this.catalogo.listarGeneros(),
    }).subscribe({
      next: (dados) => {
        this.desenvolvedoras.set(dados.desenvolvedoras.filter((d) => d.ativa));
        this.generos.set(dados.generos);
        // O backend usa a desenvolvedora vinculada à conta; o campo é só informativo.
        this.formulario.controls.desenvolvedoraId.setValue(this.desenvolvedoraVinculada()?.id ?? '');
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }

  protected ehAdmin(): boolean {
    return this.autenticacao.temPapel('ADMINISTRADOR');
  }

  protected desenvolvedoraVinculada(): Desenvolvedora | null {
    const id = this.autenticacao.usuarioAtual()?.desenvolvedoraId;
    return this.desenvolvedoras().find((d) => d.id === id) ?? null;
  }

  // ---------- Navegação ----------
  protected etapaValida(n: number): boolean {
    return this.camposDaEtapa[n].every((campo) => this.formulario.get(campo)?.valid) && this.precoValido(n);
  }

  private precoValido(n: number): boolean {
    if (n !== 2) return true;
    const { gratuito, preco } = this.formulario.getRawValue();
    if (gratuito) return true;
    return preco === '' || (!Number.isNaN(Number(preco)) && Number(preco) >= 0);
  }

  protected continuar(): void {
    this.erroEtapa.set(null);
    if (!this.etapaValida(this.etapa())) {
      this.camposDaEtapa[this.etapa()].forEach((c) => this.formulario.get(c)?.markAsTouched());
      this.erroEtapa.set(
        this.desenvolvedoraVinculada()
          ? 'Preencha os campos obrigatórios (*) desta etapa para continuar.'
          : 'Sua conta não tem desenvolvedora ativa vinculada. Peça a um administrador para vinculá-la.',
      );
      return;
    }
    this.etapa.update((e) => Math.min(4, e + 1));
  }

  protected voltar(): void {
    this.erroEtapa.set(null);
    this.etapa.update((e) => Math.max(1, e - 1));
  }

  protected irParaEtapa(n: number): void {
    for (let i = 1; i < n; i++) {
      if (!this.etapaValida(i)) return;
    }
    this.erroEtapa.set(null);
    this.etapa.set(n);
  }

  // ---------- Chips e tags ----------
  protected marcado(campo: CampoLista, valor: string): boolean {
    return this.formulario.controls[campo].value.includes(valor);
  }

  protected alternar(campo: CampoLista, valor: string): void {
    const controle = this.formulario.controls[campo];
    const atual = controle.value;
    controle.setValue(atual.includes(valor) ? atual.filter((v) => v !== valor) : [...atual, valor]);
    controle.markAsTouched();
  }

  protected escolherClassificacao(valor: string): void {
    this.formulario.controls.classificacao.setValue(valor);
  }

  protected adicionarTag(evento: Event): void {
    evento.preventDefault();
    const tag = this.entradaTag.value.trim().replace(/^#+/, '').toLowerCase().replace(/\s+/g, '');
    const tags = this.formulario.controls.tags.value;
    if (!tag || tags.includes(tag) || tags.length >= MAX_TAGS) {
      this.entradaTag.setValue('');
      return;
    }
    this.formulario.controls.tags.setValue([...tags, tag]);
    this.entradaTag.setValue('');
  }

  protected removerTag(tag: string): void {
    this.formulario.controls.tags.setValue(this.formulario.controls.tags.value.filter((t) => t !== tag));
  }

  // ---------- Mídia ----------
  protected enviarArquivo(evento: Event, destino: 'capa' | 'banner' | 'screenshot'): void {
    const input = evento.target as HTMLInputElement;
    const imagem = input.files?.[0];
    if (!imagem) return;
    this.erro.set(null);
    this.enviandoImagem.set(destino);
    this.catalogo.enviarCapa(imagem).subscribe({
      next: ({ caminho }) => {
        if (destino === 'capa') this.formulario.controls.capaUrl.setValue(caminho);
        else if (destino === 'banner') this.formulario.controls.bannerUrl.setValue(caminho);
        else this.adicionarScreenshotUrl(caminho);
        this.enviandoImagem.set(null);
        input.value = '';
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.enviandoImagem.set(null);
        input.value = '';
      },
    });
  }

  protected adicionarScreenshot(): void {
    this.adicionarScreenshotUrl(this.entradaScreenshot.value.trim());
    this.entradaScreenshot.setValue('');
  }

  private adicionarScreenshotUrl(url: string): void {
    const lista = this.formulario.controls.screenshots.value;
    if (!url || lista.includes(url) || lista.length >= MAX_SCREENSHOTS) return;
    this.formulario.controls.screenshots.setValue([...lista, url]);
  }

  protected removerScreenshot(url: string): void {
    this.formulario.controls.screenshots.setValue(this.formulario.controls.screenshots.value.filter((u) => u !== url));
  }

  // ---------- Revisão ----------
  protected nomeGenero(): string {
    return this.generos().find((g) => g.id === this.formulario.controls.generoId.value)?.nome ?? '—';
  }

  protected nomeDesenvolvedora(): string {
    return this.desenvolvedoraVinculada()?.nome ?? '—';
  }

  protected textoPreco(): string {
    const { gratuito, preco } = this.formulario.getRawValue();
    if (gratuito) return 'Gratuito';
    if (preco === '') return '—';
    return Number(preco).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  protected juntar(lista: string[], sufixo = ''): string {
    return lista.length ? lista.join(', ') + sufixo : '—';
  }

  // ---------- Envio ----------
  protected cadastrar(): void {
    if (this.enviando() || this.enviandoImagem()) return;
    for (let i = 1; i <= 3; i++) {
      if (!this.etapaValida(i)) {
        this.etapa.set(i);
        this.erroEtapa.set('Há campos obrigatórios pendentes nesta etapa.');
        return;
      }
    }

    const d = this.formulario.getRawValue();
    this.enviando.set(true);
    this.erro.set(null);
    this.sucesso.set(null);

    this.catalogo.cadastrarJogo({
      titulo: d.titulo,
      tituloOriginal: '',
      resumo: d.resumo,
      desenvolvedoraId: null, // o backend usa a desenvolvedora vinculada ao desenvolvedor
      generosIds: [d.generoId],
      dataLancamento: d.dataLancamento || null,
      capaUrl: d.capaUrl,
      trailerUrl: d.trailerUrl,
      metadados: {
        generoPrincipal: this.nomeGenero(),
        classificacaoIndicativa: d.classificacao,
        plataformas: d.plataformas,
        modosJogo: d.modosJogo,
        idiomasInterface: d.idiomas,
        precoSugerido: d.gratuito ? '0' : d.preco,
        gratuito: d.gratuito,
        tags: d.tags,
        publicadora: d.publicadora,
        siteOficial: d.siteOficial,
        bannerUrl: d.bannerUrl,
        galerias: d.screenshots,
      },
    }).subscribe({
      next: (jogo) => {
        this.enviando.set(false);
        this.sucesso.set(`"${jogo.titulo}" foi cadastrado com sucesso.`);
        this.limparRascunho();
      },
      error: (e: ErroApi) => {
        this.enviando.set(false);
        this.erro.set(e.mensagem);
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

  // ---------- Rascunho ----------
  protected limparRascunho(): void {
    try {
      localStorage.removeItem(CHAVE_RASCUNHO);
    } catch {
      /* armazenamento indisponível: ignora */
    }
    this.formulario.reset({ desenvolvedoraId: this.desenvolvedoraVinculada()?.id ?? '' });
    this.etapa.set(1);
    this.erroEtapa.set(null);
  }

  private salvarRascunho(): void {
    try {
      localStorage.setItem(CHAVE_RASCUNHO, JSON.stringify(this.formulario.getRawValue()));
    } catch {
      /* ignora */
    }
  }

  private restaurarRascunho(): void {
    try {
      const bruto = localStorage.getItem(CHAVE_RASCUNHO);
      if (bruto) this.formulario.patchValue(JSON.parse(bruto));
    } catch {
      /* rascunho corrompido: começa vazio */
    }
  }
}