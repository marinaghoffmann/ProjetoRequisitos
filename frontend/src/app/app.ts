import { Component, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { catchError, filter, map, merge, of, timer } from 'rxjs';

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

  private readonly campoBusca = viewChild<ElementRef<HTMLInputElement>>('campoBusca');
  private readonly editorJournal = viewChild(JournalEditorComponent);

  protected readonly menuConta = signal(false);
  protected readonly notificacoesNaoLidas = signal(0);

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
