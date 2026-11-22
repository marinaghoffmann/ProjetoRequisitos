import { DatePipe } from '@angular/common';
import { Component, DestroyRef, computed, effect, inject, input, signal, untracked, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, finalize, map, Observable, Subscription } from 'rxjs';

import { BibliotecaService } from '../../core/api/biblioteca.service';
import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { Pagina } from '../../core/api/forum.service';
import { EntradaJournal, JournalService } from '../../core/api/journal.service';
import { ErroApi } from '../../core/http/erro-api';
import { ConteudoModeravelComponent } from '../moderacao/conteudo-moderavel.component';
import { JournalEditorComponent } from './journal-editor.component';
import { gradientePara, imagemSegura } from '../../shared/apresentacao-jogo';
import { AvisosService } from '../../shared/ui/avisos.service';
import { CabecalhoPaginaComponent } from '../../shared/ui/cabecalho-pagina.component';
import { CarregandoComponent, EstadoVazioComponent } from '../../shared/ui/estados.component';

const STEAM_CDN = 'https://cdn.cloudflare.steamstatic.com/steam/apps';

@Component({
  selector: 'app-journal-entradas',
  imports: [DatePipe, ReactiveFormsModule, RouterLink, JournalEditorComponent, ConteudoModeravelComponent,
    CabecalhoPaginaComponent, CarregandoComponent, EstadoVazioComponent],
  templateUrl: './journal-entradas.component.html',
  styleUrl: './journal-entradas.component.scss',
})
export class JournalEntradasComponent {
  private readonly api = inject(JournalService);
  private readonly catalogo = inject(CatalogoService);
  private readonly biblioteca = inject(BibliotecaService);
  private readonly builder = inject(FormBuilder).nonNullable;
  private readonly destroyRef = inject(DestroyRef);
  private readonly avisos = inject(AvisosService);
  private consulta?: Subscription;
  readonly publico = input(false);
  readonly donoId = input<string | null>(null);
  /** true quando exibido dentro do perfil: cabeçalho compacto, no padrão das outras seções. */
  readonly embutido = input(false);
  protected readonly editor = viewChild(JournalEditorComponent);
  protected readonly jogos = signal<Pick<Jogo, 'id' | 'titulo'>[]>([]);
  protected readonly tags = signal<string[]>([]);
  protected readonly pagina = signal<Pagina<EntradaJournal> | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroCatalogo = signal<string | null>(null);
  protected readonly erroTags = signal<string | null>(null);
  protected readonly ocupada = signal<string | null>(null);
  protected readonly reveladas = signal(new Map<string, EntradaJournal>());
  /** Wallpapers HD da Steam já validados (carregaram de verdade), por jogo. */
  private readonly wallpapers = signal<Record<string, string>>({});
  private readonly wallpapersTentados = new Set<string>();
  /** Artes verticais HD da Steam já validadas, por jogo (capa em retrato do card). */
  private readonly capasVerticais = signal<Record<string, string>>({});
  private readonly capasVerticaisTentadas = new Set<string>();
  protected readonly filtros = this.builder.group({ jogoId: [''], tag: [''], inicio: [''], fim: [''], origem: [''] });

  constructor() {
    effect(() => {
      this.publico(); this.donoId();
      untracked(() => { this.carregar(1); this.carregarTags(); this.carregarJogos(); });
    });
    // Busca o wallpaper dos jogos que aparecem na página atual.
    effect(() => {
      for (const entrada of this.pagina()?.itens ?? []) {
        this.resolverWallpaper(entrada);
        this.resolverCapaVertical(entrada);
      }
    });
    // Filtros aplicados na hora, sem botão "Filtrar".
    this.filtros.valueChanges.pipe(debounceTime(250), takeUntilDestroyed()).subscribe(() => this.carregar(1));
  }

  /** Linha do tempo: entradas agrupadas pela data da sessão, na ordem que a API devolve. */
  protected readonly grupos = computed(() => {
    const grupos: { data: string; entradas: EntradaJournal[] }[] = [];
    for (const entrada of this.pagina()?.itens ?? []) {
      const data = entrada.dataSessao.slice(0, 10);
      const ultimo = grupos.at(-1);
      if (ultimo?.data === data) ultimo.entradas.push(entrada); else grupos.push({ data, entradas: [entrada] });
    }
    return grupos;
  });

  /** A capa do jogo ou, sem jogo, a primeira imagem da sessão (exceto spoiler mascarado). */
  protected capaDe(entrada: EntradaJournal): string | null {
    const capa = (entrada.jogoId && this.capasVerticais()[entrada.jogoId]) || imagemSegura(entrada.jogoCapaUrl);
    if (capa) return capa;
    return entrada.anexos.length && !entrada.mascarada ? this.imagem(entrada, entrada.anexos[0].id) : null;
  }

  /** Fundo do card: wallpaper do jogo (ou a capa dele) sob um degradê escuro que mantém o texto legível. */
  protected fundoDe(entrada: EntradaJournal): string | null {
    const imagem = (entrada.jogoId && this.wallpapers()[entrada.jogoId]) || imagemSegura(entrada.jogoCapaUrl);
    if (!imagem) return null;
    return 'linear-gradient(90deg, rgb(11 14 19 / 94%) 0%, rgb(11 14 19 / 80%) 50%, rgb(11 14 19 / 52%) 100%), '
      + `url("${imagem}") center / cover no-repeat`;
  }

  private resolverWallpaper(entrada: EntradaJournal): void {
    const jogoId = entrada.jogoId;
    if (!jogoId || this.wallpapersTentados.has(jogoId)) return;
    this.wallpapersTentados.add(jogoId);
    const m = entrada.jogoCapaUrl ? /\/apps\/(\d+)\//.exec(entrada.jogoCapaUrl) : null;
    if (!m) return;
    const base = `${STEAM_CDN}/${m[1]}`;
    const candidatas = [`${base}/library_hero.jpg`, `${base}/page_bg_generated_v6b.jpg`];
    const tentar = (i: number): void => {
      if (i >= candidatas.length) return;
      const img = new Image();
      img.onload = () => this.wallpapers.update((mapa) => ({ ...mapa, [jogoId]: candidatas[i] }));
      img.onerror = () => tentar(i + 1);
      img.src = candidatas[i];
    };
    tentar(0);
  }

  private resolverCapaVertical(entrada: EntradaJournal): void {
    const jogoId = entrada.jogoId;
    if (!jogoId || this.capasVerticaisTentadas.has(jogoId)) return;
    this.capasVerticaisTentadas.add(jogoId);
    const m = entrada.jogoCapaUrl ? /\/apps\/(\d+)\//.exec(entrada.jogoCapaUrl) : null;
    if (!m) return;
    const base = `${STEAM_CDN}/${m[1]}`;
    const candidatas = [`${base}/library_600x900_2x.jpg`, `${base}/library_600x900.jpg`];
    const tentar = (i: number): void => {
      if (i >= candidatas.length) return;
      const img = new Image();
      img.onload = () => this.capasVerticais.update((mapa) => ({ ...mapa, [jogoId]: candidatas[i] }));
      img.onerror = () => tentar(i + 1);
      img.src = candidatas[i];
    };
    tentar(0);
  }

  protected gradiente(entrada: EntradaJournal): string {
    return gradientePara(entrada.jogoId ?? entrada.id);
  }

  protected carregar(numero: number): void {
    if (this.publico() && !this.donoId()) return;
    const filtros = this.filtros.getRawValue();
    if (filtros.inicio && filtros.fim && filtros.inicio > filtros.fim) {
      this.erro.set('A data inicial não pode ser posterior à data final'); return;
    }
    this.consulta?.unsubscribe();
    this.carregando.set(true);
    this.erro.set(null);
    this.reveladas.set(new Map());
    this.consulta = this.api.entradas(filtros, numero, this.publico() ? this.donoId() : null)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (pagina) => {
          if (numero > 1 && !pagina.itens.length) { this.carregar(numero - 1); return; }
          this.pagina.set(pagina);
          this.carregando.set(false);
        },
        error: (erro: ErroApi) => { this.erro.set(erro.mensagem); this.carregando.set(false); },
      });
  }

  /**
   * Opções do filtro "Jogo". No próprio journal: só os jogos da biblioteca do usuário.
   * No perfil público ainda não há endpoint com a biblioteca de outra pessoa, então segue o catálogo.
   */
  protected carregarJogos(): void {
    this.erroCatalogo.set(null);
    const origem: Observable<Pick<Jogo, 'id' | 'titulo'>[]> = this.publico()
      ? this.catalogo.listarJogos()
      : this.biblioteca.listar().pipe(
          map((itens) => itens.map((i) => ({ id: i.jogoId, titulo: i.titulo }))
            .sort((a, b) => a.titulo.localeCompare(b.titulo))),
        );
    origem.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (jogos) => this.jogos.set(jogos),
      error: (erro: ErroApi) => this.erroCatalogo.set(erro.mensagem),
    });
  }

  protected carregarTags(): void {
    if (this.publico() && !this.donoId()) return;
    this.erroTags.set(null);
    this.api.tags(this.publico() ? this.donoId() : null).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (tags) => this.tags.set(tags), error: (erro: ErroApi) => this.erroTags.set(erro.mensagem),
    });
  }

  protected salvo(): void {
    this.avisos.sucesso('Entrada salva com sucesso'); this.carregar(1); this.carregarTags();
  }

  protected async excluir(entrada: EntradaJournal): Promise<void> {
    if (this.publico() || !entrada.editavel || this.ocupada()) return;
    const confirmado = await this.avisos.confirmar({ titulo: 'Excluir entrada',
      mensagem: `Excluir "${entrada.titulo}" e as imagens anexadas? Isso não pode ser desfeito.`, confirmar: 'Excluir', perigo: true });
    if (!confirmado) return;
    this.ocupada.set(entrada.id); this.erro.set(null);
    this.api.excluir(entrada.id).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.ocupada.set(null))).subscribe({
      next: () => { this.avisos.sucesso('Entrada excluída'); this.carregar(this.pagina()?.pagina ?? 1); this.carregarTags(); },
      error: (erro: ErroApi) => this.erro.set(erro.mensagem),
    });
  }

  protected revelar(entrada: EntradaJournal): void {
    const dono = this.donoId();
    if (!dono || this.ocupada()) return;
    this.ocupada.set(entrada.id); this.erro.set(null);
    this.api.revelar(dono, entrada.id).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.ocupada.set(null))).subscribe({
      next: (revelada) => this.reveladas.update((atuais) => new Map(atuais).set(entrada.id, revelada)),
      error: (erro: ErroApi) => this.erro.set(erro.mensagem),
    });
  }

  protected ocultar(id: string): void {
    this.reveladas.update((atuais) => { const novos = new Map(atuais); novos.delete(id); return novos; });
  }

  protected imagem(entrada: EntradaJournal, anexoId: string): string {
    return this.api.imagemUrl(entrada.id, anexoId, entrada.contemSpoiler && !entrada.mascarada);
  }
}