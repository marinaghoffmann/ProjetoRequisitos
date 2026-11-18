import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Lancamento, LancamentosService } from '../../core/api/lancamentos.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-lancamentos',
  imports: [RouterLink],
  templateUrl: './lancamentos.page.html',
  styleUrl: './lancamentos.page.scss',
})
export class LancamentosPage {
  private readonly api = inject(LancamentosService);
  protected readonly autenticacao = inject(AutenticacaoService);
  protected readonly lancamentos = signal<Lancamento[]>([]);
  protected readonly futuros = computed(() => this.lancamentos().filter(l => l.status === 'FUTURO_LANCAMENTO'));
  protected readonly lancados = computed(() => this.lancamentos().filter(l => l.status === 'LANCADO'));
  protected readonly carregando = signal(true);
  protected readonly ocupado = signal<string | null>(null);
  protected readonly erro = signal<string | null>(null);

  constructor() { this.carregar(); }

  protected data(valor: string): string { return valor.split('-').reverse().join('/'); }

  protected dataAlerta(valor: string): string {
    const [ano, mes, dia] = valor.split('-').map(Number);
    const anterior = new Date(Date.UTC(ano, mes - 1, dia - 1));
    return new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', timeZone: 'UTC' }).format(anterior);
  }

  protected quantidadeInteressados(l: Lancamento): string {
    return `${l.totalInteressados} ${l.totalInteressados === 1 ? 'interessado' : 'interessados'}`;
  }

  protected contagem(l: Lancamento): string {
    if (l.tipoData === 'ESTIMADA' && l.diasRestantes === 0) return 'Data em revisão';
    if (l.diasRestantes === 0) return 'Lança hoje';
    return `Faltam ${l.diasRestantes} ${l.diasRestantes === 1 ? 'dia' : 'dias'}`;
  }

  protected imagemUrl(caminho: string | null): string {
    if (!caminho) return '';
    return caminho.startsWith('/uploads/') || caminho.startsWith('https://') || caminho.startsWith('http://') ? caminho : '';
  }

  protected podeGerir(l: Lancamento): boolean {
    const u = this.autenticacao.usuarioAtual();
    return !!u && (u.papel === 'ADMINISTRADOR' || (u.papel === 'DESENVOLVEDOR' && u.desenvolvedoraId === l.desenvolvedoraId));
  }

  protected alternar(l: Lancamento): void {
    this.ocupado.set(l.id);
    this.erro.set(null);
    const pedido = l.alertaAtivoDoUsuario ? this.api.desmarcar(l.id) : this.api.marcar(l.id);
    pedido.subscribe({
      next: () => { this.ocupado.set(null); this.carregar(); },
      error: (e: ErroApi) => { this.ocupado.set(null); this.erro.set(e.mensagem); },
    });
  }

  private carregar(): void {
    this.carregando.set(true);
    this.api.listar(true).subscribe({
      next: l => { this.lancamentos.set(l); this.carregando.set(false); },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
  }
}
