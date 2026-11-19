import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { catchError, forkJoin, of } from 'rxjs';

import { ResumoBibliotecaService } from '../../core/api/resumo-biblioteca.service';
import { ItemWishlist, PrioridadeWishlist, ROTULOS_PRIORIDADE, WishlistService } from '../../core/api/wishlist.service';
import { JogoNaVitrine, VitrineService } from '../../core/catalogo/vitrine.service';
import { ErroApi } from '../../core/http/erro-api';
import { formatarNota, juntarMeta } from '../../shared/apresentacao-jogo';
import { AvisosService } from '../../shared/ui/avisos.service';
import { CabecalhoPaginaComponent } from '../../shared/ui/cabecalho-pagina.component';
import { CarregandoComponent, ErroComponent, EstadoVazioComponent } from '../../shared/ui/estados.component';
import { IconeComponent } from '../../shared/ui/icone.component';
import { LinhaBibliotecaComponent } from '../../shared/ui/linha-biblioteca.component';

interface LinhaWishlist extends ItemWishlist {
  meta: string;
  nota: string;
}

const ORDEM_DA_PRIORIDADE: Record<PrioridadeWishlist, number> = { ALTA: 0, MEDIA: 1, BAIXA: 2 };

/**
 * F-06: a wishlist do usuário, ordenada por prioridade (Alta, Média, Baixa; na mesma prioridade, a mais
 * recente). A ordem vem da API; ao mudar a prioridade na tela, a lista é reordenada pela mesma regra.
 */
@Component({
  selector: 'app-wishlist',
  imports: [FormsModule, CabecalhoPaginaComponent, LinhaBibliotecaComponent, CarregandoComponent, ErroComponent,
    EstadoVazioComponent, IconeComponent],
  templateUrl: './wishlist.page.html',
  styleUrl: './wishlist.page.scss',
})
export class WishlistPage {
  private readonly api = inject(WishlistService);
  private readonly vitrine = inject(VitrineService);
  private readonly resumo = inject(ResumoBibliotecaService);
  private readonly avisos = inject(AvisosService);

  protected readonly itens = signal<ItemWishlist[]>([]);
  private readonly catalogo = signal<Map<string, JogoNaVitrine>>(new Map());
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly prioridades = Object.entries(ROTULOS_PRIORIDADE) as [PrioridadeWishlist, string][];

  protected readonly linhas = computed<LinhaWishlist[]>(() => [...this.itens()]
    .sort((a, b) => ORDEM_DA_PRIORIDADE[a.prioridade] - ORDEM_DA_PRIORIDADE[b.prioridade]
      || b.adicionadoEm.localeCompare(a.adicionadoEm))
    .map((item) => {
      const jogo = this.catalogo().get(item.jogoId);
      return {
        ...item,
        meta: juntarMeta(jogo?.ano, jogo?.generos[0], jogo?.plataformas.join(', ')),
        nota: formatarNota(jogo?.nota),
      };
    }));

  constructor() {
    forkJoin({
      itens: this.api.listar(),
      jogos: this.vitrine.carregar().pipe(catchError(() => of([] as JogoNaVitrine[]))),
    }).subscribe({
      next: ({ itens, jogos }) => {
        this.itens.set(itens);
        this.catalogo.set(new Map(jogos.map((j) => [j.id, j])));
        this.carregando.set(false);
      },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
  }

  protected alterarPrioridade(item: ItemWishlist, prioridade: PrioridadeWishlist): void {
    const anterior = item.prioridade;
    this.trocar(item.jogoId, prioridade);
    this.api.alterarPrioridade(item.jogoId, prioridade).subscribe({
      next: () => this.avisos.sucesso(`Prioridade de ${item.titulo}: ${ROTULOS_PRIORIDADE[prioridade]}`),
      error: (e: ErroApi) => { this.trocar(item.jogoId, anterior); this.avisos.erro(e.mensagem); },
    });
  }

  protected remover(item: ItemWishlist): void {
    this.api.remover(item.jogoId).subscribe({
      next: () => {
        this.itens.update((lista) => lista.filter((i) => i.jogoId !== item.jogoId));
        this.resumo.atualizar();
        this.avisos.sucesso(`${item.titulo} saiu da sua wishlist`);
      },
      error: (e: ErroApi) => this.avisos.erro(e.mensagem),
    });
  }

  protected rotulo(prioridade: PrioridadeWishlist): string {
    return ROTULOS_PRIORIDADE[prioridade];
  }

  private trocar(jogoId: string, prioridade: PrioridadeWishlist): void {
    this.itens.update((lista) => lista.map((i) => (i.jogoId === jogoId ? { ...i, prioridade } : i)));
  }
}
