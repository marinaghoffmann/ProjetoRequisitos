import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';

import { CatalogoService, Genero, Jogo } from '../../core/api/catalogo.service';
import { EstatisticaComunidade, EstatisticasService } from '../../core/api/estatisticas.service';
import { ForumService, ResumoTopico } from '../../core/api/forum.service';
import { WishlistService } from '../../core/api/wishlist.service';
import { VitrineService } from '../../core/catalogo/vitrine.service';
import { formatarNota, juntarMeta, plataformasDe } from '../../shared/apresentacao-jogo';
import { AvisosService } from '../../shared/ui/avisos.service';
import { Lancamento, LancamentosService } from '../../core/api/lancamentos.service';
import { ErroApi } from '../../core/http/erro-api';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';

const STEAM_CDN = 'https://cdn.cloudflare.steamstatic.com/steam/apps';

interface Destaque {
  id: Jogo['id'];
  linha1: string;
  linha2: string;
  descricao: string;
  ano: string | null;
  capa: string | null;
  steamAppId: number | null;
}

interface JogoEmAlta {
  id: Jogo['id'];
  titulo: string;
  ano: string | null;
  capa: string | null;
  steamAppId: number | null;
}

const GRADIENTES = [
  'linear-gradient(160deg, #1b1445, #0f2b4a 55%, #3b1d5c)',
  'linear-gradient(170deg, #5a6a7a, #a45a3a 60%, #3a2418)',
  'linear-gradient(170deg, #cdbba6, #8a6f5a 55%, #3b3a3f)',
  'linear-gradient(170deg, #1b1230, #14614a 55%, #0b1a14)',
  'linear-gradient(170deg, #e59a82, #3f7a8a 60%, #1c3a3a)',
  'linear-gradient(170deg, #e8c9a8, #6a7a6a 60%, #2d3a36)',
];

@Component({
  selector: 'app-inicio-page',
  imports: [DatePipe, RouterLink],
  templateUrl: './inicio.page.html',
  styleUrl: './inicio.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InicioPage implements OnInit, OnDestroy {
  private readonly catalogo = inject(CatalogoService);
  private readonly lancamentosApi = inject(LancamentosService);
  protected readonly autenticacao = inject(AutenticacaoService);
  private readonly estatisticas = inject(EstatisticasService);
  private readonly forum = inject(ForumService);
  private readonly wishlist = inject(WishlistService);
  private readonly vitrine = inject(VitrineService);
  private readonly avisos = inject(AvisosService);
  private readonly router = inject(Router);

  /** Nota pública da F-17 (null abaixo de 3 avaliações) e jogos na wishlist do usuário. */
  private readonly notas = signal<Map<string, number | null>>(new Map());
  private readonly desejados = signal<Set<string>>(new Set());
  private readonly generos = signal<Genero[]>([]);
  /** "Da sua comunidade": os tópicos mais recentes do fórum (F-13). */
  protected readonly atividades = signal<ResumoTopico[]>([]);

  // ===== Dados vindos da API (mesmo CatalogoService do catálogo) =====
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  private readonly jogos = signal<Jogo[]>([]);

  /** Imagens HD da Steam já validadas (carregaram de verdade), por chave "hero:id" / "card:id". */
  private readonly imagensHd = signal<Record<string, string>>({});

  /** Jogos ativos, com os que têm capa válida primeiro. */
  private readonly ativos = computed(() => {
    const ativos = this.jogos().filter((j) => j.status === 'ATIVO');
    const comCapa = (j: Jogo) => (this.imagemUrl(j.capaUrl) ? 0 : 1);
    return [...ativos].sort((a, b) => comCapa(a) - comCapa(b));
  });

  /** Hero: até 4 jogos. */
  protected readonly destaques = computed<Destaque[]>(() => this.ativos().slice(0, 4).map((j) => this.paraDestaque(j)));

  /** "Em alta agora": 6 jogos. */
  protected readonly emAlta = computed<JogoEmAlta[]>(() => this.ativos().slice(0, 6).map((j) => this.paraCard(j)));

  protected readonly indice = signal(0);
  protected readonly destaqueAtual = computed(() => this.destaques()[this.indice()] ?? null);
  private temporizador?: ReturnType<typeof setInterval>;

  // A prévia usa a mesma agenda da página de lançamentos, ordenada pela API.
  protected readonly carregandoLancamentos = signal(true);
  protected readonly erroLancamentos = signal<string | null>(null);
  private readonly agenda = signal<Lancamento[]>([]);
  protected readonly emBreve = computed(() => this.agenda().filter(l => l.status === 'FUTURO_LANCAMENTO').slice(0, 3));

  ngOnInit(): void {
    this.catalogo.listarJogos().subscribe({
      next: (jogos) => {
        this.jogos.set(jogos);
        this.carregando.set(false);
        this.destaques().forEach((d) => this.resolverHero(d));
        this.emAlta().forEach((c) => this.resolverCard(c));
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });

    this.estatisticas.comunidade('TODOS').pipe(catchError(() => of([] as EstatisticaComunidade[])))
      .subscribe((fichas) => this.notas.set(new Map(fichas.map((f) => [f.jogoId, f.publica ? f.mediaGeral : null]))));
    this.catalogo.listarGeneros().pipe(catchError(() => of([] as Genero[]))).subscribe((g) => this.generos.set(g));
    this.forum.listar({ ordem: 'recentes', pagina: 1, tamanho: 3 }).pipe(catchError(() => of(null)))
      .subscribe((pagina) => this.atividades.set(pagina?.itens ?? []));
    if (this.autenticacao.usuarioAtual()) {
      this.wishlist.listar().pipe(catchError(() => of([])))
        .subscribe((itens) => this.desejados.set(new Set(itens.map((i) => i.jogoId))));
    }

    this.lancamentosApi.listar().subscribe({
      next: lancamentos => { this.agenda.set(lancamentos); this.carregandoLancamentos.set(false); },
      error: (e: ErroApi) => { this.erroLancamentos.set(e.mensagem); this.carregandoLancamentos.set(false); },
    });

    this.temporizador = setInterval(() => {
      const total = this.destaques().length;
      if (total > 1) this.indice.update((i) => (i + 1) % total);
    }, 7000);
  }

  ngOnDestroy(): void {
    clearInterval(this.temporizador);
  }

  protected diaLancamento(data: string): string { return data.slice(8, 10); }
  protected mesLancamento(data: string): string {
    const mes = Number(data.slice(5, 7));
    return new Intl.DateTimeFormat('pt-BR', { month: 'short', timeZone: 'UTC' })
      .format(new Date(Date.UTC(2026, mes - 1, 1))).replace('.', '').toUpperCase();
  }
  protected capaLancamento(l: Lancamento): string {
    const capa = this.imagemUrl(l.capaUrl);
    return capa ? `url("${capa}") center / cover no-repeat` : 'var(--surface-3)';
  }

  protected nota(jogoId: string): string {
    return formatarNota(this.notas().get(jogoId));
  }

  protected naWishlist(jogoId: string): boolean {
    return this.desejados().has(jogoId);
  }

  protected alternarWishlist(jogoId: string, titulo: string): void {
    if (!this.autenticacao.usuarioAtual()) {
      this.router.navigate(['/login']);
      return;
    }
    this.vitrine.alternarWishlist(jogoId, this.naWishlist(jogoId)).subscribe({
      next: (agora) => {
        this.desejados.update((atual) => {
          const novo = new Set(atual);
          if (agora) novo.add(jogoId); else novo.delete(jogoId);
          return novo;
        });
        this.avisos.sucesso(agora ? `${titulo} entrou na sua wishlist` : `${titulo} saiu da sua wishlist`);
      },
      error: (e: ErroApi) => this.avisos.erro(e.mensagem),
    });
  }

  protected tituloDoJogo(jogoId: string | null): string | null {
    return jogoId ? (this.jogos().find((j) => j.id === jogoId)?.titulo ?? null) : null;
  }

  protected iniciais(nome: string | null): string {
    return (nome ?? '?').split(/\s+/).slice(0, 2).map((p) => p.charAt(0)).join('').toUpperCase();
  }

  /** "Gênero · plataformas" do jogo do lançamento; sem cadastro, a data e o tipo. */
  protected metaLancamento(l: Lancamento): string {
    const jogo = this.jogos().find((j) => j.id === l.jogoId);
    const nomes = new Map(this.generos().map((g) => [g.id, g.nome]));
    const genero = jogo?.generosIds.map((id) => nomes.get(id)).find(Boolean);
    const plataformas = jogo ? plataformasDe(jogo).join(', ') : '';
    return juntarMeta(genero, plataformas) || (l.tipoData === 'ESTIMADA' ? 'Data estimada' : 'Data confirmada');
  }

  protected ir(i: number): void {
    this.indice.set(i);
  }

  protected rotulo(i: number): string {
    return String(i + 1).padStart(2, '0');
  }

  /** Mesma regra do catálogo: só aceita /uploads/ ou http(s). */
  protected imagemUrl(caminho?: string | null): string {
    if (!caminho) return '';
    if (caminho.startsWith('/uploads/')) return caminho;
    if (caminho.startsWith('http://') || caminho.startsWith('https://')) return caminho;
    return '';
  }

  protected fundoHero(d: Destaque): string {
    const img = this.imagensHd()[`hero:${d.id}`] || this.imagemUrl(d.capa);
    const base = 'linear-gradient(115deg, #070a10, #0b1018 55%, #2a0d12)';
    // O degradê de leitura à esquerda já vem do ::before do .hm-hero no SCSS.
    return img ? `url("${img}") right center / cover no-repeat, ${base}` : base;
  }

  /** Logo oficial do jogo (PNG transparente da Steam); vazio se não existir. */
  protected logoHero(d: Destaque): string {
    return this.imagensHd()[`logo:${d.id}`] ?? '';
  }

  protected fundoCard(j: JogoEmAlta, i: number): string {
    const img = this.imagensHd()[`card:${j.id}`] || this.imagemUrl(j.capa);
    return img ? `url("${img}") center / cover no-repeat` : GRADIENTES[i % GRADIENTES.length];
  }

  // ===== Imagens HD da Steam =====

  private resolverHero(d: Destaque): void {
    if (!d.steamAppId) return;
    const base = `${STEAM_CDN}/${d.steamAppId}`;
    this.resolverImagem(`hero:${d.id}`, [
      `${base}/library_hero.jpg`,
      `${base}/page_bg_generated_v6b.jpg`,
    ]);
    this.resolverImagem(`logo:${d.id}`, [
      `${base}/library_logo.png`,
      `${base}/logo.png`,
    ]);
  }

  private resolverCard(c: JogoEmAlta): void {
    if (!c.steamAppId) return;
    const base = `${STEAM_CDN}/${c.steamAppId}`;
    this.resolverImagem(`card:${c.id}`, [
      `${base}/library_600x900.jpg`,
      `${base}/library_600x900_2x.jpg`,
    ]);
  }

  /** Testa as candidatas em ordem; a primeira que carregar vira a imagem. Se nenhuma, mantém o capaUrl. */
  private resolverImagem(chave: string, candidatas: string[]): void {
    const tentar = (i: number): void => {
      if (i >= candidatas.length) return;
      const img = new Image();
      img.onload = () => this.imagensHd.update((m) => ({ ...m, [chave]: candidatas[i] }));
      img.onerror = () => tentar(i + 1);
      img.src = candidatas[i];
    };
    tentar(0);
  }

  /**
   * Descobre o appid da Steam sem precisar de campo novo no backend:
   * 1) capaUrl / trailerUrl no formato .../steam/apps/{appid}/...
   * 2) metadadosJson (steamAppId, appid, appId, steam_appid)
   */
  private steamAppId(j: Jogo): number | null {
    for (const url of [j.capaUrl, j.trailerUrl]) {
      const m = url ? /\/steam\/apps\/(\d+)\//.exec(url) ?? /\/apps\/(\d+)\//.exec(url) : null;
      if (m) return Number(m[1]);
    }
    try {
      const meta = JSON.parse(j.metadadosJson || '{}') as Record<string, unknown>;
      const v = meta['steamAppId'] ?? meta['appid'] ?? meta['appId'] ?? meta['steam_appid'];
      const n = Number(v);
      return Number.isInteger(n) && n > 0 ? n : null;
    } catch {
      return null;
    }
  }

  // ===== Mapeamento API -> tela =====
  private paraDestaque(j: Jogo): Destaque {
    const [linha1, linha2] = this.dividirTitulo(j.titulo);
    return {
      id: j.id,
      linha1,
      linha2,
      descricao: j.resumo?.trim() || 'Veja os detalhes deste jogo no catálogo do Game Tracker.',
      ano: this.ano(j.dataLancamento),
      capa: j.capaUrl ?? null,
      steamAppId: this.steamAppId(j),
    };
  }

  private paraCard(j: Jogo): JogoEmAlta {
    return {
      id: j.id,
      titulo: j.titulo,
      ano: this.ano(j.dataLancamento),
      capa: j.capaUrl ?? null,
      steamAppId: this.steamAppId(j),
    };
  }

  private ano(data?: string | null): string | null {
    return data ? String(new Date(data).getUTCFullYear()) : null;
  }

  /** "Eclipse: Fallen Order" -> ["Eclipse", "Fallen Order"]; sem ":" quebra após a 1ª palavra. */
  private dividirTitulo(titulo: string): [string, string] {
    const t = titulo.trim();
    const i = t.indexOf(':');
    if (i > 0) return [t.slice(0, i).trim(), t.slice(i + 1).trim()];
    const partes = t.split(/\s+/);
    return partes.length > 1 ? [partes[0], partes.slice(1).join(' ')] : [t, ''];
  }
}