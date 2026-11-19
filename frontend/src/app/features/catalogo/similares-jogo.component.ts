import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { JogoSimilar, SimilaridadeService, TipoFeedback } from '../../core/api/similaridade.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

const STEAM_CDN = 'https://cdn.cloudflare.steamstatic.com/steam/apps';

/**
 * "Jogos similares" da página do jogo (F-10): cards com capa, título e grau de relevância. Quem está logado
 * marca "Útil" ou "Não relevante"; clicar de novo no botão marcado remove o feedback, e clicar no outro
 * troca. Administradores adicionam uma conexão manual (jogo e grau), removem conexões e recalculam.
 */
@Component({
  selector: 'app-similares-jogo',
  imports: [RouterLink],
  templateUrl: './similares-jogo.component.html',
  styleUrl: './similares-jogo.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SimilaresJogoComponent implements OnInit {
  private readonly servico = inject(SimilaridadeService);
  private readonly catalogo = inject(CatalogoService);
  protected readonly autenticacao = inject(AutenticacaoService);

  readonly jogoId = input.required<string>();

  protected readonly similares = signal<JogoSimilar[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly notaMinima = signal<number | null>(null);

  /** Capas HD da Steam (retrato 600x900) já validadas, por id do jogo. */
  private readonly capasHd = signal<Record<string, string>>({});
  private readonly capasTentadas = new Set<string>();

  // Administração
  protected readonly jogosDoCatalogo = signal<Jogo[]>([]);
  protected readonly jogoEscolhido = signal('');
  protected readonly grau = signal('0.80');
  protected readonly erroAdmin = signal<string | null>(null);

  ngOnInit(): void {
    this.carregar();
    if (this.ehAdmin()) {
      this.catalogo.listarJogos().subscribe({
        next: (jogos) => this.jogosDoCatalogo.set(jogos.filter((jogo) => jogo.id !== this.jogoId())),
      });
    }
  }

  protected ehAdmin(): boolean {
    return this.autenticacao.temPapel('ADMINISTRADOR');
  }

  protected percentual(grau: number): string {
    return `${Math.round(grau * 100)}%`;
  }

  /** Capa HD da Steam quando existir; senão a capaUrl do cadastro; senão vazio (placeholder). */
  protected capaSrc(s: JogoSimilar): string {
    return this.capasHd()[s.jogoId] || this.imagemUrl(s.capaUrl);
  }

  protected filtrar(valor: string): void {
    this.notaMinima.set(valor ? Number(valor) : null);
    this.carregar();
  }

  /** Mesmo botão: remove o feedback; o outro botão: troca; sem feedback: registra. */
  protected feedback(similar: JogoSimilar, tipo: TipoFeedback): void {
    this.erro.set(null);
    const pedido = similar.meuFeedback === tipo
      ? this.servico.removerFeedback(similar.similaridadeId)
      : similar.meuFeedback
        ? this.servico.editarFeedback(similar.similaridadeId, tipo)
        : this.servico.registrarFeedback(similar.similaridadeId, tipo);
    pedido.subscribe({
      next: () => this.carregar(),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  protected criarManual(): void {
    this.erroAdmin.set(null);
    this.servico.criarManual(this.jogoId(), this.jogoEscolhido(), Number(this.grau())).subscribe({
      next: () => {
        this.jogoEscolhido.set('');
        this.carregar();
      },
      error: (e: ErroApi) => this.erroAdmin.set(e.mensagem),
    });
  }

  protected remover(similar: JogoSimilar): void {
    this.erroAdmin.set(null);
    this.servico.remover(similar.similaridadeId).subscribe({
      next: () => this.carregar(),
      error: (e: ErroApi) => this.erroAdmin.set(e.mensagem),
    });
  }

  protected recalcular(): void {
    this.erroAdmin.set(null);
    this.servico.recalcular(this.jogoId()).subscribe({
      next: () => this.carregar(),
      error: (e: ErroApi) => this.erroAdmin.set(e.mensagem),
    });
  }

  private carregar(): void {
    this.servico.similares(this.jogoId(), 8, this.notaMinima()).subscribe({
      next: (similares) => {
        this.similares.set(similares);
        this.carregando.set(false);
        similares.forEach((s) => this.resolverCapa(s));
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }

  // ===== Capas HD da Steam =====

  private resolverCapa(s: JogoSimilar): void {
    if (this.capasTentadas.has(s.jogoId)) return;
    this.capasTentadas.add(s.jogoId);
    const appId = this.steamAppId(s.capaUrl);
    if (!appId) return;
    const base = `${STEAM_CDN}/${appId}`;
    this.testar(s.jogoId, [`${base}/library_600x900_2x.jpg`, `${base}/library_600x900.jpg`]);
  }

  /** Testa as candidatas em ordem; a primeira que carregar vira a capa. Se nenhuma, mantém a capaUrl. */
  private testar(chave: string, candidatas: string[]): void {
    const tentar = (i: number): void => {
      if (i >= candidatas.length) return;
      const img = new Image();
      img.onload = () => this.capasHd.update((m) => ({ ...m, [chave]: candidatas[i] }));
      img.onerror = () => tentar(i + 1);
      img.src = candidatas[i];
    };
    tentar(0);
  }

  private steamAppId(url?: string | null): number | null {
    const m = url ? /\/apps\/(\d+)\//.exec(url) : null;
    return m ? Number(m[1]) : null;
  }

  /** Mesma regra do catálogo: só aceita /uploads/ ou http(s). */
  private imagemUrl(caminho?: string | null): string {
    if (!caminho) return '';
    return caminho.startsWith('/uploads/') || /^https?:\/\//.test(caminho) ? caminho : '';
  }
}