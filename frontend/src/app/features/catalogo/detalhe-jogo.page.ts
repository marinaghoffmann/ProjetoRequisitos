import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';

import { CatalogoService, Captura, Desenvolvedora, Genero, Jogo } from '../../core/api/catalogo.service';
import {
  JogandoAgoraService, JogoEmAndamento, LIMITE_JOGANDO_AGORA, Progresso, ROTULOS_PROGRESSO,
} from '../../core/api/jogando-agora.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';
import { EstatisticasJogoComponent } from './estatisticas-jogo.component';
import { ReviewsJogoComponent } from './reviews-jogo.component';

const STEAM_CDN = 'https://cdn.cloudflare.steamstatic.com/steam/apps';

@Component({
  selector: 'app-detalhe-jogo',
  imports: [RouterLink, DatePipe, EstatisticasJogoComponent, ReviewsJogoComponent],
  template: `
    <!-- Fundo do jogo (arte HD da Steam; sem appid cai na capa desfocada) -->
    @if (fundo(); as f) {
      <div class="fundo" [class.fundo--capa]="!imagens()['hero']" [style.background-image]="f" aria-hidden="true"></div>
    }

    <div class="conteudo">
      <a class="voltar" routerLink="/catalogo">← Catálogo</a>

      @if (erro()) {
        <p role="alert">{{ erro() }}</p>
      } @else if (jogo(); as j) {
        <section class="topo">
          <div class="capa">
            @if (capaSrc(); as src) { <img [src]="src" [alt]="'Capa de ' + j.titulo" /> }
          </div>

          <div class="info">
            <p class="meta">
              @if (desenvolvedora(); as dev) { {{ dev }} }
              @if (desenvolvedora() && ano()) { <span class="meta__sep">·</span> }
              @if (ano(); as a) { {{ a }} }
            </p>

            <h1 class="titulo">
              @if (logo(); as l) {
                <img class="titulo__logo" [src]="l" [alt]="j.titulo" />
              } @else {
                {{ j.titulo }}
              }
            </h1>

            @if (generoNomes().length) {
              <ul class="chips">
                @for (g of generoNomes(); track g) { <li>{{ g }}</li> }
              </ul>
            }

            <p class="resumo">{{ j.resumo }}</p>

            <div class="acoes">
              @if (autenticacao.usuarioAtual()) {
                @if (atual(); as a) {
                  <select [value]="a.progresso" (change)="alterar(j.id, $any($event.target).value)">
                    @for (p of progressos; track p) {
                      <option [value]="p">{{ rotulos[p] }}</option>
                    }
                  </select>
                  <button type="button" (click)="remover(j.id)">Parar de jogar</button>
                } @else {
                  <button type="button" class="primario" (click)="marcar(j.id)">Jogando agora</button>
                }
              } @else {
                <a routerLink="/login" class="primario">Entre para marcar</a>
              }
              <button type="button" disabled title="Em breve">Wishlist</button>
              <button type="button" disabled title="Em breve">Marcar zerado</button>
              <button type="button" disabled title="Em breve">Favoritar</button>
              @if (autenticacao.temPapel('ADMINISTRADOR', 'DESENVOLVEDOR')) {
                <a [routerLink]="['/catalogo', j.id, 'editar']">Editar</a>
              }
            </div>

            @if (erroJogando(); as m) { <p class="erro" role="alert">{{ m }}</p> }
            @if (escolhendoSubstituto()) {
              <div class="lightbox" (click)="escolhendoSubstituto.set(false)">
                <div class="modal" (click)="$event.stopPropagation()">
                  <h3>Limite de 5 jogos atingido</h3>
                  <p>Escolha qual jogo sai para entrar "{{ j.titulo }}":</p>
                  @for (e of emAndamento(); track e.jogoId) {
                    <button type="button" (click)="marcar(j.id, e.jogoId)">{{ e.titulo }}</button>
                  }
                  <button type="button" (click)="escolhendoSubstituto.set(false)">Cancelar</button>
                </div>
              </div>
            }
          </div>
        </section>

        <nav class="abas">
          <button type="button" [class.ativa]="aba() === 'geral'" (click)="irParaAba('geral')">Visão geral</button>
          <button type="button" [class.ativa]="aba() === 'reviews'" (click)="irParaAba('reviews')">Reviews</button>
          <span>Journal</span>
          <span>Listas</span><span>Fórum</span>
        </nav>

        @if (capturas().length) {
          <section class="galeria">
            @for (c of capturas(); track c.completa) {
              <button type="button" (click)="ampliada.set(c.completa)">
                <img [src]="c.miniatura" alt="Captura de tela" loading="lazy" />
              </button>
            }
          </section>
        }
        @if (ampliada(); as url) {
          <div class="lightbox" (click)="ampliada.set(null)">
            <img [src]="url" alt="Captura ampliada" />
          </div>
        }

        <section class="grade">
          <div class="principal">
            @if (j.resumo) {
              <section class="painel">
                <h2>Sobre o jogo</h2>
                <p class="sobre">{{ j.resumo }}</p>
              </section>
            }
            <section class="painel">
              <app-reviews-jogo [jogoId]="j.id" />
            </section>
          </div>

          <div class="lateral">
            <section class="painel ficha">
              <h2>Ficha técnica</h2>
              <dl>
                @if (j.dataLancamento) {
                  <div><dt>Lançamento</dt><dd>{{ j.dataLancamento | date: 'dd/MM/yyyy' }}</dd></div>
                }
                @if (desenvolvedora(); as dev) {
                  <div><dt>Desenvolvedora</dt><dd>{{ dev }}</dd></div>
                }
                @if (generoNomes().length) {
                  <div><dt>Gêneros</dt><dd>{{ generoNomes().join(', ') }}</dd></div>
                }
              </dl>
            </section>
            <app-estatisticas-jogo [jogoId]="j.id" />
          </div>
        </section>
      } @else {
        <p class="carregando">Carregando…</p>
      }
    </div>
  `,
  styleUrls: ['./detalhe-jogo.page.scss'],
})
export class DetalheJogoPage {
  protected readonly autenticacao = inject(AutenticacaoService);
  private readonly catalogo = inject(CatalogoService);
  private readonly jogando = inject(JogandoAgoraService);

  protected readonly jogo = signal<Jogo | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly capturas = signal<Captura[]>([]);
  protected readonly ampliada = signal<string | null>(null);

  private readonly generos = signal<Genero[]>([]);
  private readonly desenvolvedoras = signal<Desenvolvedora[]>([]);

  /** Imagens HD da Steam já validadas: 'hero', 'capa' e 'logo'. */
  protected readonly imagens = signal<Record<string, string>>({});

  protected readonly emAndamento = signal<JogoEmAndamento[]>([]);
  protected readonly escolhendoSubstituto = signal(false);
  protected readonly erroJogando = signal<string | null>(null);
  protected readonly progressos = Object.keys(ROTULOS_PROGRESSO) as Progresso[];
  protected readonly rotulos = ROTULOS_PROGRESSO;
  /** Abas que já funcionam: "Reviews" leva à seção de reviews da página (F-02). */
  protected readonly aba = signal<'geral' | 'reviews'>('geral');
  protected readonly atual = computed(
    () => this.emAndamento().find((e) => e.jogoId === this.jogo()?.id) ?? null,
  );

  // ===== Derivados para a tela =====
  protected readonly ano = computed(() => {
    const d = this.jogo()?.dataLancamento;
    return d ? String(new Date(d).getUTCFullYear()) : null;
  });

  protected readonly generoNomes = computed(() => {
    const ids = this.jogo()?.generosIds ?? [];
    return this.generos().filter((g) => ids.includes(g.id)).map((g) => g.nome);
  });

  protected readonly desenvolvedora = computed(() => {
    const id = this.jogo()?.desenvolvedoraId;
    return this.desenvolvedoras().find((d) => d.id === id)?.nome ?? null;
  });

  protected readonly logo = computed(() => this.imagens()['logo'] ?? '');
  protected readonly capaSrc = computed(() => this.imagens()['capa'] || this.imagemUrl(this.jogo()?.capaUrl));

  /** CSS `background-image` do fundo: arte HD, ou a capa (desfocada via CSS) como plano B. */
  protected readonly fundo = computed(() => {
    const img = this.imagens()['hero'] || this.imagemUrl(this.jogo()?.capaUrl);
    return img ? `url("${img}")` : null;
  });

  constructor() {
    const id = inject(ActivatedRoute).snapshot.paramMap.get('id') ?? '';

    this.catalogo.buscarJogo(id).subscribe({
      next: (j) => {
        this.jogo.set(j);
        this.resolverImagensSteam(j);
        this.catalogo.capturas(j.id).subscribe({ next: (c) => this.capturas.set(c), error: () => {} });
        if (this.autenticacao.usuarioAtual()) this.recarregar();
      },
      error: () => this.erro.set('Jogo não encontrado.'),
    });

    // Só enfeitam a tela; se falharem, a informação simplesmente não aparece.
    this.catalogo.listarGeneros().subscribe({ next: (g) => this.generos.set(g), error: () => {} });
    this.catalogo.listarDesenvolvedoras().subscribe({ next: (d) => this.desenvolvedoras.set(d), error: () => {} });
  }

  @HostListener('document:keydown.escape')
  protected fecharJanelas(): void {
    this.ampliada.set(null);
    this.escolhendoSubstituto.set(false);
  }

  // ===== Imagens HD da Steam =====

  private resolverImagensSteam(j: Jogo): void {
    const appId = this.steamAppId(j);
    if (!appId) return;
    const base = `${STEAM_CDN}/${appId}`;
    this.resolverImagem('hero', [`${base}/library_hero.jpg`, `${base}/page_bg_generated_v6b.jpg`]);
    this.resolverImagem('capa', [`${base}/library_600x900_2x.jpg`, `${base}/library_600x900.jpg`]);
    this.resolverImagem('logo', [`${base}/library_logo.png`, `${base}/logo.png`]);
  }

  /** Testa as candidatas em ordem; a primeira que carregar é usada. Se nenhuma, mantém o capaUrl. */
  private resolverImagem(chave: string, candidatas: string[]): void {
    const tentar = (i: number): void => {
      if (i >= candidatas.length) return;
      const img = new Image();
      img.onload = () => this.imagens.update((m) => ({ ...m, [chave]: candidatas[i] }));
      img.onerror = () => tentar(i + 1);
      img.src = candidatas[i];
    };
    tentar(0);
  }

  /** appid da Steam a partir do capaUrl / trailerUrl (.../steam/apps/{appid}/...) ou do metadadosJson. */
  private steamAppId(j: Jogo): number | null {
    for (const url of [j.capaUrl, j.trailerUrl]) {
      const m = url ? /\/apps\/(\d+)\//.exec(url) : null;
      if (m) return Number(m[1]);
    }
    try {
      const meta = JSON.parse(j.metadadosJson || '{}') as Record<string, unknown>;
      const n = Number(meta['steamAppId'] ?? meta['appid'] ?? meta['appId'] ?? meta['steam_appid']);
      return Number.isInteger(n) && n > 0 ? n : null;
    } catch {
      return null;
    }
  }

  /** Mesma regra do catálogo: só aceita /uploads/ ou http(s). */
  private imagemUrl(caminho?: string | null): string {
    if (!caminho) return '';
    if (caminho.startsWith('/uploads/')) return caminho;
    if (caminho.startsWith('http://') || caminho.startsWith('https://')) return caminho;
    return '';
  }

  // ===== Jogando agora =====

  protected irParaAba(aba: 'geral' | 'reviews'): void {
    this.aba.set(aba);
    const alvo = aba === 'reviews' ? document.getElementById('reviews') : document.querySelector('.topo');
    alvo?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  private recarregar(): void {
    this.jogando.listar().subscribe({ next: (l) => this.emAndamento.set(l) });
  }

  protected marcar(id: string, substituir?: string): void {
    if (!substituir && this.emAndamento().length >= LIMITE_JOGANDO_AGORA) {
      this.escolhendoSubstituto.set(true);
      return;
    }
    this.erroJogando.set(null);
    this.jogando.marcar(id, substituir).subscribe({
      next: () => { this.escolhendoSubstituto.set(false); this.recarregar(); },
      error: (e: ErroApi) => this.erroJogando.set(e.mensagem),
    });
  }

  protected alterar(id: string, progresso: Progresso): void {
    this.jogando.alterarProgresso(id, progresso).subscribe({
      next: () => this.recarregar(),
      error: (e: ErroApi) => this.erroJogando.set(e.mensagem),
    });
  }

  protected remover(id: string): void {
    this.jogando.remover(id).subscribe({ next: () => this.recarregar() });
  }
}