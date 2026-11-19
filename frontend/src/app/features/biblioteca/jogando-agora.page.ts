import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { catchError, forkJoin, of } from 'rxjs';

import {
  JogandoAgoraService, JogoEmAndamento, LIMITE_JOGANDO_AGORA, Progresso, ROTULOS_PROGRESSO,
} from '../../core/api/jogando-agora.service';
import { ResumoBibliotecaService } from '../../core/api/resumo-biblioteca.service';
import { JogoNaVitrine, VitrineService } from '../../core/catalogo/vitrine.service';
import { ErroApi } from '../../core/http/erro-api';
import { juntarMeta } from '../../shared/apresentacao-jogo';
import { AvisosService } from '../../shared/ui/avisos.service';
import { CabecalhoPaginaComponent } from '../../shared/ui/cabecalho-pagina.component';
import { CarregandoComponent, ErroComponent, EstadoVazioComponent } from '../../shared/ui/estados.component';
import { LinhaBibliotecaComponent } from '../../shared/ui/linha-biblioteca.component';

/**
 * F-07: jogos em andamento (até 5, RN 1), com o progresso e o instante de início (data e hora, RN 3).
 * "Concluir" e "Remover" tiram o jogo do destaque (a API não distingue os dois); o registro do zerado é a F-11.
 */
@Component({
  selector: 'app-jogando-agora',
  imports: [DatePipe, FormsModule, CabecalhoPaginaComponent, LinhaBibliotecaComponent, CarregandoComponent,
    ErroComponent, EstadoVazioComponent],
  templateUrl: './jogando-agora.page.html',
  styleUrl: './jogando-agora.page.scss',
})
export class JogandoAgoraPage {
  private readonly servico = inject(JogandoAgoraService);
  private readonly vitrine = inject(VitrineService);
  private readonly resumo = inject(ResumoBibliotecaService);
  private readonly avisos = inject(AvisosService);

  protected readonly limite = LIMITE_JOGANDO_AGORA;
  protected readonly rotulos = ROTULOS_PROGRESSO;
  protected readonly progressos = Object.keys(ROTULOS_PROGRESSO) as Progresso[];
  protected readonly jogos = signal<JogoEmAndamento[]>([]);
  private readonly catalogo = signal<Map<string, JogoNaVitrine>>(new Map());
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly subtitulo = computed(() =>
    `${this.jogos().length} de ${this.limite} espaços usados`);

  constructor() {
    forkJoin({
      jogos: this.servico.listar(),
      catalogo: this.vitrine.carregar().pipe(catchError(() => of([] as JogoNaVitrine[]))),
    }).subscribe({
      next: ({ jogos, catalogo }) => {
        this.jogos.set(jogos);
        this.catalogo.set(new Map(catalogo.map((j) => [j.id, j])));
        this.carregando.set(false);
      },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
  }

  /** "ano · gênero"; a data e a hora de início entram no template com o DatePipe. */
  protected meta(jogoId: string): string {
    const jogo = this.catalogo().get(jogoId);
    return juntarMeta(jogo?.ano, jogo?.generos[0]);
  }

  protected alterar(jogo: JogoEmAndamento, progresso: Progresso): void {
    this.servico.alterarProgresso(jogo.jogoId, progresso).subscribe({
      next: () => {
        this.jogos.update((lista) => lista.map((j) => (j.jogoId === jogo.jogoId ? { ...j, progresso } : j)));
        this.avisos.sucesso(`${jogo.titulo}: ${this.rotulos[progresso]}`);
      },
      error: (e: ErroApi) => this.avisos.erro(e.mensagem),
    });
  }

  protected remover(jogo: JogoEmAndamento, concluido: boolean): void {
    this.servico.remover(jogo.jogoId).subscribe({
      next: () => {
        this.jogos.update((lista) => lista.filter((j) => j.jogoId !== jogo.jogoId));
        this.resumo.atualizar();
        this.avisos.sucesso(concluido
          ? `${jogo.titulo} saiu do destaque. Registre o zerado em Zerados.`
          : `${jogo.titulo} saiu do destaque`);
      },
      error: (e: ErroApi) => this.avisos.erro(e.mensagem),
    });
  }
}
