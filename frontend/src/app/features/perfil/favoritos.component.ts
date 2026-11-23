import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, effect, inject, input, output, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { finalize, Observable } from 'rxjs';

import { BibliotecaService } from '../../core/api/biblioteca.service';
import { Favorito, PerfilPublicoService } from '../../core/api/perfil-publico.service';
import { ErroApi } from '../../core/http/erro-api';

const STEAM_CDN = 'https://cdn.cloudflare.steamstatic.com/steam/apps';

/** Opção do seletor: só jogos que o usuário tem na biblioteca. */
interface OpcaoJogo { id: string; titulo: string; }

@Component({
  selector: 'app-favoritos',
  imports: [RouterLink],
  templateUrl: './favoritos.component.html',
  styleUrl: './favoritos.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FavoritosComponent {
  private readonly api = inject(PerfilPublicoService);
  private readonly biblioteca = inject(BibliotecaService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');

  readonly favoritos = input.required<Favorito[]>();
  readonly editavel = input(false);
  readonly alterado = output<void>();

  protected readonly slots = [1, 2, 3, 4];
  protected readonly jogos = signal<OpcaoJogo[] | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroCatalogo = signal<string | null>(null);
  protected readonly ocupada = signal(false);
  protected readonly carregando = signal(false);
  protected readonly substituindo = signal<number | null>(null);
  protected readonly selecionado = signal('');

  /** Artes verticais HD da Steam já validadas, por jogo. */
  private readonly capasHd = signal<Record<string, string>>({});
  private readonly capasTentadas = new Set<string>();
  /** true = capa em retrato (preenche o quadro); false = quadrada/horizontal (inteira, sobre fundo desfocado). */
  private readonly formas = signal<Record<string, boolean>>({});

  constructor() {
    effect(() => this.favoritos().forEach((f) => this.resolverCapa(f)));
  }

  protected capaSrc(f: Favorito): string {
    return this.capasHd()[f.jogoId] || f.capaUrl || '';
  }

  protected ajustada(f: Favorito): boolean {
    return this.formas()[f.jogoId] === false;
  }

  protected aoCarregar(evento: Event, jogoId: string): void {
    const img = evento.target as HTMLImageElement;
    if (!img.naturalWidth || !img.naturalHeight) return;
    const retrato = img.naturalWidth / img.naturalHeight <= 0.8;
    if (this.formas()[jogoId] !== retrato) this.formas.update((m) => ({ ...m, [jogoId]: retrato }));
  }

  protected abrir(slot?: number): void {
    this.substituindo.set(slot ?? null);
    this.selecionado.set('');
    this.erro.set(null);
    this.dialogo().nativeElement.showModal();
    this.carregarJogos();
  }

  protected fechar(): void {
    this.dialogo().nativeElement.close();
  }

  /** Carrega a biblioteca a cada abertura (ela pode ter mudado desde a última vez). */
  protected carregarJogos(): void {
    if (this.carregando()) return;
    this.carregando.set(true);
    this.erroCatalogo.set(null);
    this.biblioteca.listar().pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.carregando.set(false)),
    ).subscribe({
      next: (itens) => this.jogos.set(
        itens.map((i) => ({ id: i.jogoId, titulo: i.titulo })).sort((a, b) => a.titulo.localeCompare(b.titulo)),
      ),
      error: (erro: ErroApi) => this.erroCatalogo.set(erro.mensagem),
    });
  }

  protected jaFavorito(id: string): boolean {
    return this.favoritos().some((jogo) => jogo.jogoId === id);
  }

  protected salvar(): void {
    const jogoId = this.selecionado();
    if (!jogoId || this.ocupada()) return;
    const slot = this.substituindo();
    this.executar(slot ? this.api.substituir(slot, jogoId) : this.api.adicionar(jogoId), true);
  }

  protected mover(jogoId: string, slot: string): void {
    this.executar(this.api.mover(jogoId, Number(slot)));
  }

  protected remover(jogoId: string): void {
    this.executar(this.api.remover(jogoId));
  }

  private executar(requisicao: Observable<void>, fechar = false): void {
    if (this.ocupada()) return;
    this.ocupada.set(true);
    this.erro.set(null);
    requisicao.pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.ocupada.set(false)),
    ).subscribe({
      next: () => {
        if (fechar) this.fechar();
        this.alterado.emit();
      },
      error: (erro: ErroApi) => this.erro.set(erro.mensagem),
    });
  }

  // ===== Arte vertical da Steam =====

  private resolverCapa(f: Favorito): void {
    const chave = `${f.jogoId}|${f.capaUrl ?? ''}`;
    if (this.capasTentadas.has(chave)) return;
    this.capasTentadas.add(chave);
    const m = f.capaUrl ? /\/apps\/(\d+)\//.exec(f.capaUrl) : null;
    if (!m) return;
    const base = `${STEAM_CDN}/${m[1]}`;
    const candidatas = [`${base}/library_600x900_2x.jpg`, `${base}/library_600x900.jpg`];
    const tentar = (i: number): void => {
      if (i >= candidatas.length) return;
      const img = new Image();
      img.onload = () => this.capasHd.update((mapa) => ({ ...mapa, [f.jogoId]: candidatas[i] }));
      img.onerror = () => tentar(i + 1);
      img.src = candidatas[i];
    };
    tentar(0);
  }
}
