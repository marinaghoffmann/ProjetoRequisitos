import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { ErroApi } from '../../core/http/erro-api';

interface Destaque {
  id: Jogo['id'];
  linha1: string;
  linha2: string;
  descricao: string;
  ano: string | null;
  capa: string | null;
}

interface JogoEmAlta {
  id: Jogo['id'];
  titulo: string;
  ano: string | null;
  capa: string | null;
}

interface Lancamento {
  dia: string;
  mes: string;
  titulo: string;
  meta: string;
  capa: string;
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

  // ===== Dados vindos da API (mesmo CatalogoService do catálogo) =====
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  private readonly jogos = signal<Jogo[]>([]);

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

  // ===== Ainda mockado (não há endpoint para isso) =====
  protected readonly emBreve: Lancamento[] = [
    { dia: '15', mes: 'NOV', titulo: 'Starforge', meta: 'RPG · PC, PS5', capa: 'linear-gradient(170deg, #5a8fc0, #d6a23a)' },
    { dia: '23', mes: 'NOV', titulo: 'Titanfall: Reborn', meta: 'FPS · PC, Xbox', capa: 'linear-gradient(170deg, #1a1d22, #343a44)' },
    { dia: '09', mes: 'DEZ', titulo: 'The Last Signal', meta: 'Narrativo · PC, PS5', capa: 'linear-gradient(160deg, #1b1445, #3b1d7a 60%, #0f4a5a)' },
  ];
  protected readonly miniaturasLista = GRADIENTES.slice(0, 4);
  protected readonly gradienteResenha = GRADIENTES[0];

  ngOnInit(): void {
    this.catalogo.listarJogos().subscribe({
      next: (jogos) => {
        this.jogos.set(jogos);
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });

    this.temporizador = setInterval(() => {
      const total = this.destaques().length;
      if (total > 1) this.indice.update((i) => (i + 1) % total);
    }, 7000);
  }

  ngOnDestroy(): void {
    clearInterval(this.temporizador);
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
    const img = this.imagemUrl(d.capa);
    const base = 'linear-gradient(115deg, #070a10, #0b1018 55%, #2a0d12)';
    return img ? `url("${img}") center / cover no-repeat, ${base}` : base;
  }

  protected fundoCard(j: JogoEmAlta, i: number): string {
    const img = this.imagemUrl(j.capa);
    return img ? `url("${img}") center / cover no-repeat` : GRADIENTES[i % GRADIENTES.length];
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
    };
  }

  private paraCard(j: Jogo): JogoEmAlta {
    return { id: j.id, titulo: j.titulo, ano: this.ano(j.dataLancamento), capa: j.capaUrl ?? null };
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