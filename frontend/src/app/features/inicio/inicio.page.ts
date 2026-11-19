import { ChangeDetectionStrategy, Component, ElementRef, HostListener, OnDestroy, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
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

interface ResultadoBusca {
  id: Jogo['id'];
  titulo: string;
  ano: string | null;
  capa: string | null;
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
  imports: [RouterLink],
  templateUrl: './inicio.page.html',
  styleUrl: './inicio.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InicioPage implements OnInit, OnDestroy {
  private readonly catalogo = inject(CatalogoService);
  private readonly lancamentosApi = inject(LancamentosService);
  private readonly router = inject(Router);
  protected readonly autenticacao = inject(AutenticacaoService);

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
  protected readonly miniaturasLista = GRADIENTES.slice(0, 4);
  protected readonly gradienteResenha = GRADIENTES[0];

  // ===== Busca =====
  private readonly campoBusca = viewChild<ElementRef<HTMLInputElement>>('busca');

  protected readonly buscaAberta = signal(false);
  protected readonly termo = signal('');
  protected readonly selecionadoBusca = signal(0);

  /** Jogos ativos filtrados pelo termo (sem diferenciar acentos/maiúsculas), até 6 resultados. */
  protected readonly resultadosBusca = computed<ResultadoBusca[]>(() => {
    const t = this.normalizar(this.termo());
    if (!t) return [];
    return this.ativos()
      .filter((j) => this.normalizar(j.titulo).includes(t))
      .slice(0, 6)
      .map((j) => {
        const appId = this.steamAppId(j);
        return {
          id: j.id,
          titulo: j.titulo,
          ano: this.ano(j.dataLancamento),
          capa:
            this.imagensHd()[`card:${j.id}`] ||
            this.imagemUrl(j.capaUrl) ||
            (appId ? `${STEAM_CDN}/${appId}/header.jpg` : null),
        };
      });
  });

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

  // ===== Busca: métodos =====

  protected digitar(valor: string): void {
    this.termo.set(valor);
    this.selecionadoBusca.set(0);
    this.buscaAberta.set(true);
  }

  protected tecladoBusca(e: KeyboardEvent): void {
    const total = this.resultadosBusca().length;
    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        if (total) this.selecionadoBusca.update((i) => (i + 1) % total);
        break;
      case 'ArrowUp':
        e.preventDefault();
        if (total) this.selecionadoBusca.update((i) => (i - 1 + total) % total);
        break;
      case 'Enter': {
        const r = this.resultadosBusca()[this.selecionadoBusca()];
        if (r) {
          e.preventDefault();
          this.router.navigate(['/catalogo', r.id]);
          this.fecharBusca(true);
        }
        break;
      }
      case 'Escape':
        this.fecharBusca();
        this.campoBusca()?.nativeElement.blur();
        break;
    }
  }

  protected fecharBusca(limpar = false): void {
    this.buscaAberta.set(false);
    if (limpar) {
      this.termo.set('');
      this.selecionadoBusca.set(0);
      const campo = this.campoBusca()?.nativeElement;
      if (campo) campo.value = '';
    }
  }

  /** Fecha a lista ao clicar fora da caixa de busca. */
  @HostListener('document:click', ['$event'])
  protected cliqueFora(e: MouseEvent): void {
    if (!(e.target as HTMLElement | null)?.closest('.hm-busca-caixa')) this.fecharBusca();
  }

  /** Atalho ⌘K / Ctrl+K foca a busca. */
  @HostListener('document:keydown', ['$event'])
  protected atalhoBusca(e: KeyboardEvent): void {
    if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
      e.preventDefault();
      this.campoBusca()?.nativeElement.focus();
    }
  }

  private normalizar(s: string): string {
    return s.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
  }

  // ===== Lançamentos =====

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
    // Cards horizontais: capsule 616x353 (melhor resolução) e header.jpg como alternativa.
    this.resolverImagem(`card:${c.id}`, [
      `${base}/capsule_616x353.jpg`,
      `${base}/header.jpg`,
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