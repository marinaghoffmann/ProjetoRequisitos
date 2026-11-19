import { Component, DestroyRef, ElementRef, inject, input, output, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { finalize, Observable } from 'rxjs';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { Favorito, PerfilPublicoService } from '../../core/api/perfil-publico.service';
import { ErroApi } from '../../core/http/erro-api';

const STEAM_CDN = 'https://cdn.cloudflare.steamstatic.com/steam/apps';

@Component({
  selector: 'app-favoritos',
  imports: [RouterLink],
  templateUrl: './favoritos.component.html',
  styleUrl: './favoritos.component.scss',
})
export class FavoritosComponent {
  private readonly api = inject(PerfilPublicoService);
  private readonly catalogo = inject(CatalogoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');

  readonly favoritos = input.required<Favorito[]>();
  readonly editavel = input(false);
  readonly alterado = output<void>();

  protected readonly slots = [1, 2, 3, 4];
  protected readonly jogos = signal<Jogo[] | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroCatalogo = signal<string | null>(null);
  protected readonly ocupada = signal(false);
  protected readonly carregando = signal(false);
  protected readonly substituindo = signal<number | null>(null);
  protected readonly selecionado = signal('');

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

  protected carregarJogos(): void {
    if (this.jogos() || this.carregando()) return;
    this.carregando.set(true);
    this.erroCatalogo.set(null);
    this.catalogo.listarJogos().pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.carregando.set(false)),
    ).subscribe({
      next: (jogos) => this.jogos.set(jogos),
      error: (erro: ErroApi) => this.erroCatalogo.set(erro.mensagem),
    });
  }

  /** Capa vertical da Steam (mesma da página do jogo), com o capaUrl como plano B. */
  protected candidatas(favorito: Favorito): string[] {
    const appId = favorito.capaUrl ? /\/apps\/(\d+)\//.exec(favorito.capaUrl)?.[1] : null;
    const steam = appId
      ? [`${STEAM_CDN}/${appId}/library_600x900_2x.jpg`, `${STEAM_CDN}/${appId}/library_600x900.jpg`]
      : [];
    return [...steam, ...(favorito.capaUrl ? [favorito.capaUrl] : [])];
  }

  /** Tenta a próxima candidata; se acabarem, esconde a imagem. */
  protected falhar(imagem: HTMLImageElement, favorito: Favorito): void {
    const lista = this.candidatas(favorito);
    const proxima = Number(imagem.dataset['i'] ?? 0) + 1;
    if (proxima < lista.length) {
      imagem.dataset['i'] = String(proxima);
      imagem.src = lista[proxima];
    } else {
      imagem.hidden = true;
    }
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
}