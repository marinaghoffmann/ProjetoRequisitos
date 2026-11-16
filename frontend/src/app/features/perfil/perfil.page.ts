import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import {
  JogandoAgoraService, JogoEmAndamento, LIMITE_JOGANDO_AGORA, Progresso, ROTULOS_PROGRESSO,
} from '../../core/api/jogando-agora.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';
import { PerfilListasComponent } from './perfil-listas.component';
import { PerfilReviewsComponent } from './perfil-reviews.component';

const ROTULOS_PAPEL: Record<string, string> = {
  COMUM: 'Player',
  DESENVOLVEDOR: 'Dev',
  MODERADOR: 'Mod',
  ADMINISTRADOR: 'Admin',
};

const GRADIENTES = [
  'linear-gradient(160deg, #1b1445, #0f2b4a 55%, #3b1d5c)',
  'linear-gradient(170deg, #5a6a7a, #a45a3a 60%, #3a2418)',
  'linear-gradient(170deg, #1b2a4a, #c88a4a 60%, #14212f)',
  'linear-gradient(170deg, #1b1230, #14614a 55%, #0b1a14)',
  'linear-gradient(170deg, #e59a82, #3f7a8a 60%, #1c3a3a)',
];

@Component({
  selector: 'app-perfil-page',
  imports: [RouterLink, PerfilReviewsComponent, PerfilListasComponent],
  templateUrl: './perfil.page.html',
  styleUrl: './perfil.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerfilPage implements OnInit {
  protected readonly autenticacao = inject(AutenticacaoService);
  private readonly jogando = inject(JogandoAgoraService);

  // ===== Dados reais =====
  protected readonly usuario = computed(() => this.autenticacao.usuarioAtual());
  protected readonly iniciais = computed(() => (this.usuario()?.nome ?? '?').slice(0, 2).toUpperCase());
  protected readonly papel = computed(() => {
    const p = this.usuario()?.papel ?? '';
    return ROTULOS_PAPEL[p] ?? p;
  });

  protected readonly avatar = computed(() => this.imagem(this.usuario()?.avatarUrl));
  protected readonly banner = computed(() => this.imagem(this.usuario()?.bannerUrl));

  protected readonly emAndamento = signal<JogoEmAndamento[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  protected readonly limite = LIMITE_JOGANDO_AGORA;
  protected readonly progressos = Object.keys(ROTULOS_PROGRESSO) as Progresso[];
  protected readonly rotulos = ROTULOS_PROGRESSO;

  // ===== Ainda não existe backend para estes blocos (aparecem como "Fazendo") =====
  protected readonly abas = ['Journal', 'Biblioteca'];
  /** Aba exibida: "Atividade" (padrão) ou uma das que já funcionam (Reviews, F-02; Listas, F-04). */
  protected readonly aba = signal('Atividade');
  protected readonly estatisticas = ['jogos', 'zerados', 'seguidores', 'seguindo'];
  protected readonly favoritos = [1, 2, 3, 4];
  protected readonly atividades = [1, 2];
  protected readonly barras = [30, 48, 36, 62, 44, 70, 40, 52, 60, 38, 66, 46];

  ngOnInit(): void {
    this.recarregar();
  }

  /** Imagem enviada pelo usuário; nulo mantém o degradê padrão do CSS. */
  private imagem(url: string | null | undefined): string | null {
    return url ? `url("${encodeURI(url)}") center / cover no-repeat` : null;
  }

  protected gradiente(i: number): string {
    return GRADIENTES[i % GRADIENTES.length];
  }

  /** Preenchimento da barra segundo a posição do progresso na lista (ordem do enum). */
  protected percentual(p: Progresso): number {
    const i = this.progressos.indexOf(p);
    return i < 0 ? 0 : Math.round(((i + 1) / this.progressos.length) * 100);
  }

  protected alterar(jogoId: string, progresso: string): void {
    this.erro.set(null);
    this.jogando.alterarProgresso(jogoId, progresso as Progresso).subscribe({
      next: () => this.recarregar(),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  protected remover(jogoId: string): void {
    this.erro.set(null);
    this.jogando.remover(jogoId).subscribe({
      next: () => this.recarregar(),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  private recarregar(): void {
    this.jogando.listar().subscribe({
      next: (lista) => {
        this.emAndamento.set(lista);
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }

  /** Valor da variável CSS --capa (imagem da API); "none" mantém só o degradê. */
  protected capa(e: JogoEmAndamento): string {
    return e.capaUrl ? `url("${encodeURI(e.capaUrl)}")` : 'none';
  }
}