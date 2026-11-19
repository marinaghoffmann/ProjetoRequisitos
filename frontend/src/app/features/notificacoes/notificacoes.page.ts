import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { Notificacao, NotificacoesService } from '../../core/api/notificacoes.service';
import { ErroApi } from '../../core/http/erro-api';
import { AvisosService } from '../../shared/ui/avisos.service';
import { CabecalhoPaginaComponent } from '../../shared/ui/cabecalho-pagina.component';
import { CarregandoComponent, ErroComponent, EstadoVazioComponent } from '../../shared/ui/estados.component';
import { IconeComponent } from '../../shared/ui/icone.component';
import { NomeIcone } from '../../shared/ui/icones';

const ICONE_POR_TIPO: Record<Notificacao['tipo'], NomeIcone> = {
  LANCAMENTO_PROXIMO: 'sino',
  LANCAMENTO_ADIADO: 'timeline',
  ADVERTENCIA: 'moderacao',
  MODERACAO: 'moderacao',
};

/** Avisos internos (F-12 e F-14). Abrir uma notificação a marca como lida; os lançamentos levam à agenda. */
@Component({
  selector: 'app-notificacoes',
  imports: [DatePipe, CabecalhoPaginaComponent, CarregandoComponent, ErroComponent, EstadoVazioComponent, IconeComponent],
  template: `
    <main class="pagina pagina--estreita">
      <gt-cabecalho-pagina titulo="Notificações"
        [subtitulo]="naoLidas() ? naoLidas() + (naoLidas() === 1 ? ' não lida' : ' não lidas') : 'Tudo em dia'">
        @if (naoLidas()) { <button acao type="button" class="botao-contorno" (click)="marcarTodas()">Marcar como lidas</button> }
      </gt-cabecalho-pagina>

      @if (carregando()) {
        <gt-carregando tipo="linhas" [quantidade]="4" />
      } @else if (erro(); as mensagem) {
        <gt-erro [mensagem]="mensagem" />
      } @else if (!notificacoes().length) {
        <gt-estado-vazio icone="sino" titulo="Nenhuma notificação"
          texto="Avisos de lançamentos que você acompanha e da moderação aparecem aqui." />
      } @else {
        <ul class="lista">
          @for (n of notificacoes(); track n.id) {
            <li>
              <button type="button" class="notificacao" [class.notificacao--lida]="n.lidaEm" (click)="abrir(n)">
                <span class="notificacao__icone"><gt-icone [nome]="icone(n)" /></span>
                <span class="notificacao__textos">
                  <strong>{{ n.titulo }}</strong>
                  <span>{{ n.mensagem }}</span>
                  <time [attr.datetime]="n.geradaEm">{{ n.geradaEm | date: "d 'de' MMM, HH:mm" }}</time>
                </span>
                @if (!n.lidaEm) { <i class="notificacao__ponto" aria-label="Não lida"></i> }
                <gt-icone nome="chevron" [tamanho]="16" />
              </button>
            </li>
          }
        </ul>
      }
    </main>
  `,
  styles: `
    .lista { display: grid; gap: 8px; margin: 0; padding: 0; list-style: none; }
    .notificacao { display: flex; align-items: center; justify-content: flex-start; gap: 14px; width: 100%; min-height: 0; padding: 14px 16px;
      border: 1px solid var(--line-soft); border-radius: 12px; background: var(--surface); color: var(--muted); text-align: left; white-space: normal; }
    .notificacao:hover { border-color: var(--line); color: var(--text); filter: none; transform: none; }
    .notificacao--lida { opacity: 0.6; }
    .notificacao__icone { display: grid; flex: 0 0 auto; place-items: center; width: 38px; height: 38px; border-radius: 50%;
      background: var(--surface-2); color: var(--accent); }
    .notificacao__textos { display: flex; flex: 1; flex-direction: column; gap: 2px; min-width: 0; font-weight: 500; font-size: 12.5px; }
    strong { color: var(--text); font-size: 13.5px; }
    time { color: var(--faint); font-size: 11px; }
    .notificacao__ponto { flex: 0 0 auto; width: 8px; height: 8px; border-radius: 50%; background: var(--accent); }
  `,
})
export class NotificacoesPage {
  private readonly api = inject(NotificacoesService);
  private readonly router = inject(Router);
  private readonly avisos = inject(AvisosService);

  protected readonly notificacoes = signal<Notificacao[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly naoLidas = computed(() => this.notificacoes().filter((n) => !n.lidaEm).length);

  constructor() {
    this.api.listar().subscribe({
      next: (n) => { this.notificacoes.set(n); this.carregando.set(false); },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
  }

  protected icone(n: Notificacao): NomeIcone {
    return ICONE_POR_TIPO[n.tipo] ?? 'sino';
  }

  protected abrir(n: Notificacao): void {
    if (!n.lidaEm) this.marcarLida(n);
    if (n.tipo === 'LANCAMENTO_PROXIMO' || n.tipo === 'LANCAMENTO_ADIADO') this.router.navigate(['/lancamentos']);
  }

  /** Não há rota em lote: marca uma a uma. */
  protected marcarTodas(): void {
    const pendentes = this.notificacoes().filter((n) => !n.lidaEm);
    forkJoin(pendentes.map((n) => this.api.marcarLida(n.id))).subscribe({
      next: (atuais) => {
        const porId = new Map(atuais.map((a) => [a.id, a]));
        this.notificacoes.update((lista) => lista.map((item) => porId.get(item.id) ?? item));
        this.avisos.sucesso('Notificações marcadas como lidas');
      },
      error: (e: ErroApi) => this.avisos.erro(e.mensagem),
    });
  }

  private marcarLida(n: Notificacao): void {
    this.api.marcarLida(n.id).subscribe({
      next: (atual) => this.notificacoes.update((lista) => lista.map((item) => (item.id === atual.id ? atual : item))),
      error: (e: ErroApi) => this.avisos.erro(e.mensagem),
    });
  }
}
