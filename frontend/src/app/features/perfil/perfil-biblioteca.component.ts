import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, effect, inject, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, of, Subject, switchMap } from 'rxjs';
import { BibliotecaService, ItemBiblioteca } from '../../core/api/biblioteca.service';
import { CatalogoService } from '../../core/api/catalogo.service';
import { ErroApi } from '../../core/http/erro-api';

interface JogoBusca { id: string; titulo: string; capaUrl: string | null; }

const STEAM_CDN = 'https://cdn.cloudflare.steamstatic.com/steam/apps';
const GRADIENTE = 'linear-gradient(170deg, #1b1445, #0f2b4a 55%, #3b1d5c)';

/** Aba "Biblioteca" do perfil: jogos que o usuário já tem, escolhidos entre os cadastrados no catálogo. */
@Component({
  selector: 'app-perfil-biblioteca',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './perfil-biblioteca.component.scss',
  template: `
    <section class="pf-secao">
      <div class="pf-secao__cab">
        <div>
          <h2>Biblioteca</h2>
          <p>{{ itens().length }} {{ itens().length === 1 ? 'jogo' : 'jogos' }}</p>
        </div>
        <button type="button" class="pf-btn" (click)="alternarBusca()">
          {{ buscando() ? 'Fechar' : 'Adicionar jogo' }}
        </button>
      </div>

      @if (erro(); as mensagem) {
        <p class="pf-aviso" role="alert">{{ mensagem }}</p>
      }

      @if (buscando()) {
        <div class="bib-busca">
          <input
            #campo
            type="search"
            class="bib-busca__campo"
            placeholder="Buscar jogo no catálogo…"
            aria-label="Buscar jogo no catálogo"
            (input)="termo$.next(campo.value)"
          />
          @if (buscaCarregando()) {
            <p class="pf-estado">Buscando…</p>
          } @else if (termoAtual().length >= 2 && !resultados().length) {
            <p class="pf-estado">Nenhum jogo encontrado no catálogo.</p>
          }
          <ul class="bib-resultados">
            @for (j of resultados(); track j.id) {
              <li>
                <span class="bib-mini" [style.background]="fundo(capaSrc(j.id, j.capaUrl))"></span>
                <span class="bib-resultados__titulo">{{ j.titulo }}</span>
                @if (possui(j.id)) {
                  <span class="pf-estado">Já na biblioteca</span>
                } @else {
                  <button type="button" class="pf-btn" (click)="adicionar(j)">Adicionar</button>
                }
              </li>
            }
          </ul>
        </div>
      }

      @if (carregando()) {
        <p class="pf-estado">Carregando…</p>
      } @else if (!itens().length) {
        <p class="pf-estado">Sua biblioteca está vazia. Use "Adicionar jogo" para marcar os jogos que você já tem.</p>
      } @else {
        <ul class="pf-jogando">
          @for (item of itens(); track item.jogoId) {
            <li class="pf-jogo">
              <a class="pf-jogo__capa" [routerLink]="['/catalogo', item.jogoId]"
                 [style.--capa]="capa(capaSrc(item.jogoId, item.capaUrl))" [style.background]="gradiente"
                 [attr.aria-label]="'Abrir ' + item.titulo"></a>
              <div class="pf-jogo__corpo">
                <a class="pf-jogo__titulo" [routerLink]="['/catalogo', item.jogoId]">{{ item.titulo }}</a>
                <button type="button" class="pf-link" (click)="remover(item.jogoId)">Remover da biblioteca</button>
              </div>
            </li>
          }
        </ul>
      }
    </section>
  `,
})
export class PerfilBibliotecaComponent implements OnInit {
  private readonly servico = inject(BibliotecaService);
  private readonly catalogo = inject(CatalogoService);
  private readonly destroyRef = inject(DestroyRef);

  /** Avisa o perfil sempre que o total de jogos da biblioteca muda. */
  readonly totalAlterado = output<number>();

  protected readonly gradiente = GRADIENTE;
  protected readonly itens = signal<ItemBiblioteca[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly buscando = signal(false);
  protected readonly buscaCarregando = signal(false);
  protected readonly termoAtual = signal('');
  protected readonly resultados = signal<JogoBusca[]>([]);
  protected readonly termo$ = new Subject<string>();

  /** Artes verticais HD da Steam já validadas, por jogo (mesma técnica dos favoritos). */
  private readonly capasHd = signal<Record<string, string>>({});
  private readonly capasTentadas = new Set<string>();

  constructor() {
    effect(() => {
      this.itens().forEach((i) => this.resolverCapa(i.jogoId, i.capaUrl));
      this.resultados().forEach((j) => this.resolverCapa(j.id, j.capaUrl));
    });
  }

  ngOnInit(): void {
    this.carregar();
    this.termo$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((termo) => {
          this.termoAtual.set(termo.trim());
          if (termo.trim().length < 2) {
            this.resultados.set([]);
            return of(null);
          }
          this.buscaCarregando.set(true);
          return this.catalogo.listarJogos(termo.trim());
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (resposta) => {
          if (resposta !== null) this.resultados.set(resposta.slice(0, 20));
          this.buscaCarregando.set(false);
        },
        error: (e: ErroApi) => {
          this.erro.set(e.mensagem);
          this.buscaCarregando.set(false);
        },
      });
  }

  private carregar(): void {
    this.servico.listar().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (lista) => { this.itens.set(lista); this.carregando.set(false); this.totalAlterado.emit(lista.length); },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
  }

  protected alternarBusca(): void {
    this.buscando.update((v) => !v);
    this.resultados.set([]);
    this.termoAtual.set('');
  }

  protected possui(jogoId: string): boolean {
    return this.itens().some((i) => i.jogoId === jogoId);
  }

  protected adicionar(jogo: JogoBusca): void {
    this.erro.set(null);
    this.servico.adicionar(jogo.id).subscribe({
      next: (item) => { this.itens.update((l) => [item, ...l]); this.totalAlterado.emit(this.itens().length); },
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  protected remover(jogoId: string): void {
    this.erro.set(null);
    this.servico.remover(jogoId).subscribe({
      next: () => { this.itens.update((l) => l.filter((i) => i.jogoId !== jogoId)); this.totalAlterado.emit(this.itens().length); },
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  /** Arte vertical HD quando existir; senão, a capa que veio da API. */
  protected capaSrc(jogoId: string, capaUrl: string | null): string | null {
    return this.capasHd()[jogoId] || capaUrl;
  }

  private resolverCapa(jogoId: string, capaUrl: string | null): void {
    const chave = `${jogoId}|${capaUrl ?? ''}`;
    if (this.capasTentadas.has(chave)) return;
    this.capasTentadas.add(chave);
    const m = capaUrl ? /\/apps\/(\d+)\//.exec(capaUrl) : null;
    if (!m) return;
    const base = `${STEAM_CDN}/${m[1]}`;
    const candidatas = [`${base}/library_600x900_2x.jpg`, `${base}/library_600x900.jpg`];
    const tentar = (i: number): void => {
      if (i >= candidatas.length) return;
      const img = new Image();
      img.onload = () => this.capasHd.update((mapa) => ({ ...mapa, [jogoId]: candidatas[i] }));
      img.onerror = () => tentar(i + 1);
      img.src = candidatas[i];
    };
    tentar(0);
  }

  protected capa(url: string | null): string {
    return url ? `url("${encodeURI(url)}")` : 'none';
  }

  protected fundo(url: string | null): string {
    return url ? `url("${encodeURI(url)}") center / cover no-repeat` : GRADIENTE;
  }
}