import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { BuscaService, ResultadoBusca, TAMANHO_MINIMO_DO_TERMO } from '../../core/api/busca.service';
import { CatalogoService, Genero } from '../../core/api/catalogo.service';
import { ErroApi } from '../../core/http/erro-api';
import { anoDe, gradientePara, imagemSegura } from '../../shared/apresentacao-jogo';
import { CabecalhoPaginaComponent } from '../../shared/ui/cabecalho-pagina.component';
import { CarregandoComponent, ErroComponent, EstadoVazioComponent } from '../../shared/ui/estados.component';
import { IconeComponent } from '../../shared/ui/icone.component';

/**
 * Busca global (/busca?termo=): jogos e listas agrupados. Sem termo, mostra atalhos para os gêneros do
 * catálogo. Não há busca de pessoas nem de reviews: a API não tem consulta pública para isso.
 */
@Component({
  selector: 'app-busca',
  imports: [RouterLink, CabecalhoPaginaComponent, CarregandoComponent, ErroComponent, EstadoVazioComponent, IconeComponent],
  templateUrl: './busca.page.html',
  styleUrl: './busca.page.scss',
})
export class BuscaPage {
  private readonly api = inject(BuscaService);
  private readonly router = inject(Router);

  protected readonly termo = signal('');
  protected readonly resultado = signal<ResultadoBusca | null>(null);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly generos = toSignal(
    inject(CatalogoService).listarGeneros().pipe(catchError(() => of([] as Genero[]))), { initialValue: [] });
  protected readonly total = computed(() => (this.resultado()?.jogos.length ?? 0) + (this.resultado()?.listas.length ?? 0));
  protected readonly termoCurto = computed(() => this.termo().length > 0 && this.termo().length < TAMANHO_MINIMO_DO_TERMO);

  constructor() {
    inject(ActivatedRoute).queryParamMap.pipe(map((p) => (p.get('termo') ?? '').trim()), takeUntilDestroyed())
      .subscribe((termo) => this.buscar(termo));
  }

  protected pesquisar(termo: string): void {
    this.router.navigate([], { queryParams: termo.trim() ? { termo: termo.trim() } : {} });
  }

  protected capa(url: string | null, id: string): { imagem: string | null; fundo: string } {
    return { imagem: imagemSegura(url), fundo: gradientePara(id) };
  }

  protected ano(data: string | null): string | null {
    return anoDe(data);
  }

  private buscar(termo: string): void {
    this.termo.set(termo);
    this.resultado.set(null);
    this.erro.set(null);
    if (termo.length < TAMANHO_MINIMO_DO_TERMO) return;
    this.carregando.set(true);
    this.api.buscar(termo).subscribe({
      next: (r) => { this.resultado.set(r); this.carregando.set(false); },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
  }
}
