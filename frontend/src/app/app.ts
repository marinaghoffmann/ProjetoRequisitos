import { Component, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Subject, catchError, debounceTime, distinctUntilChanged, filter, map, merge, of, switchMap, timer } from 'rxjs';

import { CatalogoService, Jogo } from './core/api/catalogo.service';
import { NaoLidasService } from './core/api/nao-lidas.service';
import { NotificacoesService } from './core/api/notificacoes.service';
import { ResumoBibliotecaService } from './core/api/resumo-biblioteca.service';
import { AutenticacaoService } from './core/auth/autenticacao.service';
import { JournalEditorComponent } from './features/journal/journal-editor.component';
import { imagemSegura } from './shared/apresentacao-jogo';
import { AvisosComponent } from './shared/ui/avisos.component';
import { AvisosService } from './shared/ui/avisos.service';
import { IconeComponent } from './shared/ui/icone.component';
import { NomeIcone } from './shared/ui/icones';

const STEAM_CDN = 'https://cdn.cloudflare.steamstatic.com/steam/apps';

/** Intervalo das contagens do menu e do sino (F-19 RN 11 e notificações). */
const ATUALIZAR_CONTADORES_A_CADA_MS = 30000;

interface ItemMenu {
  rotulo: string;
  rota: string;
  icone: NomeIcone;
  contador?: number;
  /** Rota ativa só no caminho exato (padrão) ou também nos filhos. */
  exato?: boolean;
  queryParams?: Record<string, string>;
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, IconeComponent, AvisosComponent, JournalEditorComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  host: { '(document:keydown)': 'atalho($event)' },
})
export class App {
  protected readonly autenticacao = inject(AutenticacaoService);
  private readonly router = inject(Router);
  private readonly naoLidas = inject(NaoLidasService);
  private readonly notificacoes = inject(NotificacoesService);
  private readonly biblioteca = inject(ResumoBibliotecaService);
  private readonly avisos = inject(AvisosService);
  private readonly catalogo = inject(CatalogoService);
  private readonly termos = new Subject<string>();

  private readonly campoBusca = viewChild<ElementRef<HTMLInputElement>>('campoBusca');
  private readonly editorJournal = viewChild(JournalEditorComponent);

  protected readonly menuConta = signal(false);
  protected readonly notificacoesNaoLidas = signal(0);

  /** Busca com sugestões (capa vertical + nome do jogo) enquanto digita. */
  protected readonly aberta = signal(false);
  protected readonly termoAtual = signal('');
  protected readonly buscando = signal(false);
  protected readonly resultados = signal<Jogo[]>([]);
  protected readonly indice = signal(-1);
  /** Jogos sem capa vertical na Steam: usam a capa do cadastro. */
  protected readonly verticaisAusentes = signal<Set<string>>(new Set());

  /**
   * Ordem do protótipo. Lançamentos fica em "Em breve → Todos" na Início e Notificações no sino: as rotas
   * continuam existindo. Comunidade não tem badge porque a API não informa "novidades" do fórum.
   */
  protected readonly principal = computed<ItemMenu[]>(() => [
    { rotulo: 'Início', rota: '/inicio', icone: 'inicio' },
    { rotulo: 'Descobrir', rota: '/descobrir', icone: 'descobrir' },
    { rotulo: 'Jogos', rota: '/catalogo', icone: 'jogos' },
    { rotulo: 'Listas', rota: '/biblioteca/listas', icone: 'listas' },
    { rotulo: 'Journal', rota: '/journal', icone: 'journal' },
    { rotulo: 'Comunidade', rota: '/forum', icone: 'comunidade', exato: false },
    { rotulo: 'Marketplace', rota: '/marketplace', icone: 'marketplace' },
    { rotulo: 'Mensagens', rota: '/marketplace/mensagens', icone: 'mensagens', exato: false, contador: this.naoLidas.total() },
    { rotulo: 'Estatísticas', rota: '/estatisticas', icone: 'estatisticas' },
  ]);

  /** Totais reais de /api/perfis/me/resumo; o visitante vê os itens sem contador. */
  protected readonly minhaBiblioteca = computed<(ItemMenu & { ponto?: boolean })[]>(() => {
    const resumo = this.autenticacao.usuarioAtual() ? this.biblioteca.resumo() : null;
    return [
      { rotulo: 'Em andamento', rota: '/biblioteca/jogando-agora', icone: 'inicio', ponto: true, contador: resumo?.emAndamento },
      { rotulo: 'Wishlist', rota: '/biblioteca/wishlist', icone: 'coracao', contador: resumo?.naWishlist },
      { rotulo: 'Zerados', rota: '/biblioteca/zerados', icone: 'check', contador: resumo?.jogosZerados },
      { rotulo: 'Timeline', rota: '/timeline', icone: 'timeline' },
    ];
  });

  protected readonly porPapel = computed<ItemMenu[]>(() => [
    ...(this.autenticacao.temPapel('DESENVOLVEDOR', 'ADMINISTRADOR')
      ? [{ rotulo: 'Gerenciar catálogo', rota: '/catalogo', icone: 'catalogo' as const, queryParams: { gerenciar: '1' } }]
      : []),
    ...(this.autenticacao.temPapel('MODERADOR', 'ADMINISTRADOR')
      ? [{ rotulo: 'Moderação', rota: '/moderacao', icone: 'moderacao' as const }]
      : []),
  ]);

  /** Barra inferior abaixo de 700 px. */
  protected readonly navegacaoInferior: ItemMenu[] = [
    { rotulo: 'Início', rota: '/inicio', icone: 'inicio' },
    { rotulo: 'Descobrir', rota: '/descobrir', icone: 'descobrir' },
    { rotulo: 'Jogos', rota: '/catalogo', icone: 'jogos' },
    { rotulo: 'Listas', rota: '/biblioteca/listas', icone: 'listas' },
    { rotulo: 'Perfil', rota: '/perfil', icone: 'perfil' },
  ];

  constructor() {
    const navegacoes = this.router.events.pipe(filter((evento) => evento instanceof NavigationEnd));
    merge(timer(0, ATUALIZAR_CONTADORES_A_CADA_MS), navegacoes)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.atualizarContadores());

    this.termos
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap((termo) => {
          if (termo.length < 2) { this.buscando.set(false); return of<Jogo[]>([]); }
          this.buscando.set(true);
          return this.catalogo.listarJogos(termo).pipe(catchError(() => of<Jogo[]>([])));
        }),
        takeUntilDestroyed(),
      )
      .subscribe((jogos) => {
        this.resultados.set(jogos.slice(0, 6));
        this.buscando.set(false);
        this.indice.set(-1);
      });
  }

  protected aoDigitar(valor: string): void {
    const termo = valor.trim();
    this.termoAtual.set(termo);
    this.aberta.set(true);
    if (termo.length >= 2) this.buscando.set(true);
    this.termos.next(termo);
  }

  /** Setas escolhem a sugestão, Enter abre o jogo escolhido e Esc fecha a lista. */
  protected teclado(evento: KeyboardEvent): void {
    const total = this.resultados().length;
    if (evento.key === 'Escape') { this.aberta.set(false); return; }
    if (!this.aberta() || !total) return;
    if (evento.key === 'ArrowDown') {
      evento.preventDefault();
      this.indice.update((i) => (i + 1) % total);
    } else if (evento.key === 'ArrowUp') {
      evento.preventDefault();
      this.indice.update((i) => (i <= 0 ? total - 1 : i - 1));
    }
  }

  /** Enter/lupa: abre o jogo destacado; sem destaque, vai para a página de busca completa. */
  protected submeter(termo: string): void {
    const jogo = this.resultados()[this.indice()];
    if (this.aberta() && jogo) {
      this.router.navigate(['/catalogo', jogo.id]);
      this.selecionar();
    } else {
      this.aberta.set(false);
      this.buscar(termo);
    }
  }

  /** Depois de abrir um resultado, fecha a lista e limpa o campo. */
  protected selecionar(): void {
    this.aberta.set(false);
    this.termoAtual.set('');
    this.indice.set(-1);
    const campo = this.campoBusca()?.nativeElement;
    if (campo) campo.value = '';
  }

  /** Capa vertical HD da Steam; sem ela (ou sem appid), a capa do cadastro. */
  protected capaVertical(jogo: Jogo): string {
    const m = jogo.capaUrl ? /\/apps\/(\d+)\//.exec(jogo.capaUrl) : null;
    if (m && !this.verticaisAusentes().has(jogo.id)) return `${STEAM_CDN}/${m[1]}/library_600x900.jpg`;
    return this.imagemUrl(jogo.capaUrl);
  }

  protected semVertical(jogoId: string): void {
    this.verticaisAusentes.update((atual) => new Set(atual).add(jogoId));
  }

  protected ano(jogo: Jogo): string {
    return jogo.dataLancamento?.slice(0, 4) ?? '';
  }

  /** Ctrl/⌘+K foca a busca de qualquer tela. */
  protected atalho(evento: KeyboardEvent): void {
    if ((evento.metaKey || evento.ctrlKey) && evento.key.toLowerCase() === 'k') {
      evento.preventDefault();
      this.campoBusca()?.nativeElement.focus();
    }
  }

  protected buscar(termo: string): void {
    const limpo = termo.trim();
    this.router.navigate(['/busca'], { queryParams: limpo ? { termo: limpo } : {} });
  }

  /** "+ Registrar": a mesma F-15 do Journal, com as mesmas regras do editor. */
  protected registrar(): void {
    this.editorJournal()?.abrir();
  }

  protected entradaSalva(): void {
    this.avisos.sucesso('Entrada salva no seu Journal');
  }

  protected sair(): void {
    this.menuConta.set(false);
    this.autenticacao.sair();
  }

  protected imagemUrl(caminho?: string | null): string {
    return imagemSegura(caminho) ?? '';
  }

  private atualizarContadores(): void {
    this.naoLidas.atualizar();
    this.biblioteca.atualizar();
    if (!this.autenticacao.usuarioAtual()) {
      this.notificacoesNaoLidas.set(0);
      return;
    }
    this.notificacoes
      .listar()
      .pipe(map((lista) => lista.filter((n) => !n.lidaEm).length), catchError(() => of(0)))
      .subscribe((total) => this.notificacoesNaoLidas.set(total));
  }
}