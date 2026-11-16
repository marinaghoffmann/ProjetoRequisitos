import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Lancamento, LancamentosService } from '../../core/api/lancamentos.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-lancamentos',
  imports: [RouterLink],
  template: `
    <main class="pagina">
      <header><div><a routerLink="/catalogo">← Catálogo</a><h1>Lançamentos futuros</h1>
        <p>Acompanhe os jogos que chegam em breve.</p></div>
        @if (autenticacao.temPapel('ADMINISTRADOR', 'DESENVOLVEDOR')) {
          <a class="botao" routerLink="/lancamentos/novo">Cadastrar lançamento</a>
        }
      </header>
      @if (erro()) { <p role="alert" class="erro">{{ erro() }}</p> }
      @if (carregando()) { <p>Carregando lançamentos…</p> }
      @else if (!lancamentos().length) { <p>Nenhum lançamento futuro cadastrado.</p> }
      @else {
        <div class="grade">
          @for (l of lancamentos(); track l.id) {
            <article>
              @if (l.capaUrl) { <img [src]="l.capaUrl" [alt]="'Capa de ' + l.titulo" /> }
              <div><h2><a [routerLink]="['/catalogo', l.jogoId]">{{ l.titulo }}</a></h2>
                <p>{{ l.tipoData === 'ESTIMADA' ? 'Possível lançamento' : 'Lançamento confirmado' }}: {{ data(l.dataPrevista) }}</p>
                @if (l.tipoData === 'ESTIMADA' && l.diasRestantes === 0) {
                  <strong>Estimativa vencida; aguardando atualização</strong>
                } @else {
                  <strong>{{ l.diasRestantes }} {{ l.diasRestantes === 1 ? 'dia' : 'dias' }} restantes</strong>
                }
                @if (l.tipoData === 'ESTIMADA') {
                  <p>Data ainda não confirmada. O aviso de 24 horas será ativado quando ela for confirmada.</p>
                }
                @if (autenticacao.usuarioAtual()) {
                  <button type="button" [disabled]="ocupado() === l.id" (click)="alternar(l)">
                    {{ l.alertaAtivoDoUsuario ? 'Desmarcar interesse' : 'Quero acompanhar' }}
                  </button>
                } @else { <a routerLink="/login">Entre para acompanhar este jogo</a> }
                @if (podeGerir(l)) { <a [routerLink]="['/lancamentos', l.id, 'editar']">Editar cronograma</a> }
              </div>
            </article>
          }
        </div>
      }
    </main>
  `,
  styles: [`
    .pagina{max-width:1100px;margin:auto;padding:32px}.pagina header{display:flex;justify-content:space-between;gap:20px;align-items:start}
    .grade{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:20px;margin-top:24px}
    article{background:#171c28;border:1px solid #30394a;border-radius:16px;overflow:hidden}
    article img{width:100%;height:190px;object-fit:cover}article div{padding:18px;display:grid;gap:8px}
    h2{margin:0}button,.botao{background:#7c5cff;color:white;border:0;padding:10px 14px;border-radius:8px;cursor:pointer}
    a{color:#b7a8ff}.erro{color:#ff9a9a}
  `],
})
export class LancamentosPage {
  private readonly api = inject(LancamentosService);
  protected readonly autenticacao = inject(AutenticacaoService);
  protected readonly lancamentos = signal<Lancamento[]>([]);
  protected readonly carregando = signal(true);
  protected readonly ocupado = signal<string | null>(null);
  protected readonly erro = signal<string | null>(null);
  constructor() { this.carregar(); }
  protected data(valor: string): string { return valor.split('-').reverse().join('/'); }
  protected podeGerir(l: Lancamento): boolean {
    const u = this.autenticacao.usuarioAtual();
    return !!u && (u.papel === 'ADMINISTRADOR' || (u.papel === 'DESENVOLVEDOR' && u.desenvolvedoraId === l.desenvolvedoraId));
  }
  protected alternar(l: Lancamento): void {
    this.ocupado.set(l.id); this.erro.set(null);
    const pedido = l.alertaAtivoDoUsuario ? this.api.desmarcar(l.id) : this.api.marcar(l.id);
    pedido.subscribe({
      next: () => { this.ocupado.set(null); this.carregar(); },
      error: (e: ErroApi) => { this.ocupado.set(null); this.erro.set(e.mensagem); },
    });
  }
  private carregar(): void {
    this.api.listar().subscribe({
      next: l => { this.lancamentos.set(l); this.carregando.set(false); },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
  }
}
