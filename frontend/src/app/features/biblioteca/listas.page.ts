import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import {
  ItemDaLista, LIMITE_DESCRICAO_LISTA, LIMITE_JOGOS_LISTA, LIMITE_TITULO_LISTA, ListaPersonalizada, ListasService,
} from '../../core/api/listas.service';
import { ErroApi } from '../../core/http/erro-api';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { gradientePara, imagemSegura } from '../../shared/apresentacao-jogo';
import { AvisosService } from '../../shared/ui/avisos.service';
import { CabecalhoPaginaComponent } from '../../shared/ui/cabecalho-pagina.component';
import { CarregandoComponent, ErroComponent, EstadoVazioComponent } from '../../shared/ui/estados.component';
import { ModalComponent } from '../../shared/ui/modal.component';

const STEAM_CDN = 'https://cdn.cloudflare.steamstatic.com/steam/apps';

/**
 * "Minhas listas" (F-04): as listas do usuário com o total de jogos, mais criar, renomear (título e
 * descrição) e excluir, num modal. RN 1 (título repetido) e o limite de 100 jogos vêm da API.
 */
@Component({
  selector: 'app-listas',
  imports: [RouterLink, FormsModule, CabecalhoPaginaComponent, CarregandoComponent, ErroComponent,
    EstadoVazioComponent, ModalComponent],
  templateUrl: './listas.page.html',
  styleUrl: './listas.page.scss',
})
export class ListasPage {
  private readonly servico = inject(ListasService);
  private readonly avisos = inject(AvisosService);
  protected readonly autenticacao = inject(AutenticacaoService);

  protected readonly limiteJogos = LIMITE_JOGOS_LISTA;
  protected readonly limiteTitulo = LIMITE_TITULO_LISTA;
  protected readonly limiteDescricao = LIMITE_DESCRICAO_LISTA;

  protected readonly listas = signal<ListaPersonalizada[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly abertas = signal<Set<string>>(new Set());

  /** Wallpapers HD da Steam já validados (carregaram de verdade), por jogo. */
  private readonly wallpapers = signal<Record<string, string>>({});
  private readonly wallpapersTentados = new Set<string>();

  /** Modal de criar (editandoId null) ou renomear. */
  protected readonly modalAberto = signal(false);
  protected readonly editandoId = signal<string | null>(null);
  protected readonly titulo = signal('');
  protected readonly descricao = signal('');
  protected readonly erroModal = signal<string | null>(null);
  protected readonly salvando = signal(false);

  constructor() {
    this.carregar();
  }

  protected abrirCriacao(): void {
    this.editandoId.set(null);
    this.titulo.set('');
    this.descricao.set('');
    this.erroModal.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(lista: ListaPersonalizada): void {
    this.editandoId.set(lista.id);
    this.titulo.set(lista.titulo);
    this.descricao.set(lista.descricao);
    this.erroModal.set(null);
    this.modalAberto.set(true);
  }

  protected salvar(): void {
    if (this.salvando() || !this.titulo().trim()) return;
    const id = this.editandoId();
    const dados = { titulo: this.titulo(), descricao: this.descricao() };
    this.salvando.set(true);
    this.erroModal.set(null);
    (id ? this.servico.editar(id, dados) : this.servico.criar(dados)).subscribe({
      next: () => {
        this.salvando.set(false);
        this.modalAberto.set(false);
        this.avisos.sucesso(id ? 'Lista atualizada' : 'Lista criada');
        this.carregar();
      },
      error: (e: ErroApi) => {
        this.salvando.set(false);
        this.erroModal.set(e.mensagem);
      },
    });
  }

  protected async excluir(lista: ListaPersonalizada): Promise<void> {
    const confirmado = await this.avisos.confirmar({ titulo: 'Excluir lista',
      mensagem: `Excluir "${lista.titulo}" e todos os seus itens?`, confirmar: 'Excluir', perigo: true });
    if (!confirmado) return;
    this.servico.excluir(lista.id).subscribe({
      next: () => { this.avisos.sucesso('Lista excluída'); this.carregar(); },
      error: (e: ErroApi) => this.avisos.erro(e.mensagem),
    });
  }

  protected alternar(id: string): void {
    this.abertas.update((atual) => {
      const novo = new Set(atual);
      if (novo.has(id)) novo.delete(id); else novo.add(id);
      return novo;
    });
  }

  /** Leque de até quatro capas; sem imagem, o gradiente do jogo. */
  protected leque(lista: ListaPersonalizada): { imagem: string | null; fundo: string }[] {
    return lista.itens.slice(0, 4).map((item) => ({
      imagem: imagemSegura(item.jogoCapaUrl), fundo: gradientePara(item.jogoId),
    }));
  }

  /** Fundo do card: wallpaper do primeiro jogo (ou a capa dele) sob um degradê escuro que mantém o texto legível. */
  protected fundoDe(lista: ListaPersonalizada): string | null {
    const primeiro = lista.itens[0];
    if (!primeiro) return null;
    const imagem = this.wallpapers()[primeiro.jogoId] || imagemSegura(primeiro.jogoCapaUrl);
    if (!imagem) return null;
    return 'linear-gradient(90deg, rgb(11 14 19 / 94%) 0%, rgb(11 14 19 / 80%) 50%, rgb(11 14 19 / 52%) 100%), '
      + `url("${imagem}") center / cover no-repeat`;
  }

  protected inicial(nome: string | null): string {
    return (nome ?? '?').charAt(0).toUpperCase();
  }

  private carregar(): void {
    this.servico.minhas().subscribe({
      next: (listas) => {
        this.listas.set(listas);
        this.carregando.set(false);
        listas.forEach((l) => l.itens[0] && this.resolverWallpaper(l.itens[0]));
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }

  private resolverWallpaper(item: ItemDaLista): void {
    if (this.wallpapersTentados.has(item.jogoId)) return;
    this.wallpapersTentados.add(item.jogoId);
    const m = item.jogoCapaUrl ? /\/apps\/(\d+)\//.exec(item.jogoCapaUrl) : null;
    if (!m) return;
    const base = `${STEAM_CDN}/${m[1]}`;
    const candidatas = [`${base}/library_hero.jpg`, `${base}/page_bg_generated_v6b.jpg`];
    const tentar = (i: number): void => {
      if (i >= candidatas.length) return;
      const img = new Image();
      img.onload = () => this.wallpapers.update((mapa) => ({ ...mapa, [item.jogoId]: candidatas[i] }));
      img.onerror = () => tentar(i + 1);
      img.src = candidatas[i];
    };
    tentar(0);
  }
}