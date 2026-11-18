import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CatalogoService, Jogo } from '../../core/api/catalogo.service';
import { Lancamento, LancamentosService, TipoDataLancamento } from '../../core/api/lancamentos.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-lancamento-form',
  imports: [FormsModule, RouterLink],
  template: `
    <main class="pagina">
      <a routerLink="/lancamentos">← Lançamentos</a>
      <h1>{{ id ? 'Editar lançamento' : 'Cadastrar lançamento' }}</h1>
      @if (erro()) { <p role="alert" class="erro">{{ erro() }}</p> }
      @if (carregando()) { <p>Carregando…</p> }
      @else {
        <form (ngSubmit)="salvar()">
          @if (id) { <p>Jogo: <strong>{{ lancamento()?.titulo }}</strong></p> }
          @else {
            @if (!jogos().length) { <p>Nenhum jogo ativo disponível para cadastro.</p> }
            <label for="jogo">Jogo do catálogo</label>
            <select id="jogo" name="jogo" [(ngModel)]="jogoId" required>
              <option value="">Selecione um jogo</option>
              @for (j of jogos(); track j.id) { <option [value]="j.id">{{ j.titulo }}</option> }
            </select>
          }
          <label for="data">Data prevista</label>
          <input id="data" name="data" type="date" [(ngModel)]="dataPrevista" required />
          <label for="tipoData">Confirmação da data</label>
          <select id="tipoData" name="tipoData" [(ngModel)]="tipoData" required>
            <option value="ESTIMADA">Possível data — ainda sem confirmação</option>
            <option value="CONFIRMADA">Data informada como confirmada</option>
          </select>
          @if (tipoData === 'ESTIMADA') {
            <p>O jogo aparecerá na agenda como estimativa. Os avisos de 24 horas aguardam uma data confirmada.</p>
          }
          <button type="submit" [disabled]="salvando() || !dataPrevista || (!id && !jogoId)">Salvar</button>
          @if (id) { <button type="button" class="perigo" (click)="excluir()">Excluir lançamento</button> }
        </form>
        @if (lancamento()?.historico?.length) {
          <h2>Histórico de alterações</h2>
          <ul>
            @for (h of lancamento()!.historico; track h.id) {
              <li>{{ h.dataAnterior }} → {{ h.novaData }} ({{ h.alteradoEm }})</li>
            }
          </ul>
        }
      }
    </main>
  `,
  styles: [`
    .pagina{max-width:680px;margin:auto;padding:32px}form{display:grid;gap:12px}
    input,select{padding:10px;background:#171c28;color:white;border:1px solid #596277;border-radius:8px}
    button{padding:10px;background:#7c5cff;color:white;border:0;border-radius:8px;cursor:pointer}
    .perigo{background:#863e4a}.erro{color:#ff9a9a}a{color:#b7a8ff}
  `],
})
export class LancamentoFormPage {
  private readonly api = inject(LancamentosService);
  private readonly catalogo = inject(CatalogoService);
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected readonly autenticacao = inject(AutenticacaoService);
  protected readonly id = this.rota.snapshot.paramMap.get('id');
  protected readonly jogos = signal<Jogo[]>([]);
  protected readonly lancamento = signal<Lancamento | null>(null);
  protected readonly carregando = signal(true);
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected jogoId = '';
  protected dataPrevista = '';
  protected tipoData: TipoDataLancamento = 'ESTIMADA';
  constructor() {
    if (this.id) this.api.buscar(this.id).subscribe({
      next: l => { this.lancamento.set(l); this.jogoId = l.jogoId; this.dataPrevista = l.dataPrevista; this.tipoData = l.tipoData; this.carregando.set(false); },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
    else forkJoin({ jogos: this.catalogo.listarJogos(), lancamentos: this.api.listar() }).subscribe({
      next: ({ jogos, lancamentos }) => {
        const usuario = this.autenticacao.usuarioAtual();
        const inscritos = new Set(lancamentos.map(l => l.jogoId));
        this.jogos.set(jogos.filter(j => j.status === 'ATIVO' && !inscritos.has(j.id)
          && (usuario?.papel === 'ADMINISTRADOR'
            || (usuario?.papel === 'DESENVOLVEDOR' && usuario.desenvolvedoraId === j.desenvolvedoraId))));
        this.carregando.set(false);
      },
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
    });
  }
  protected salvar(): void {
    this.salvando.set(true); this.erro.set(null);
    const pedido = this.id ? this.api.alterar(this.id, this.dataPrevista, this.tipoData)
      : this.api.cadastrar(this.jogoId, this.dataPrevista, this.tipoData);
    pedido.subscribe({
      next: () => this.router.navigate(['/lancamentos']),
      error: (e: ErroApi) => { this.erro.set(e.mensagem); this.salvando.set(false); },
    });
  }
  protected excluir(): void {
    if (!this.id || !confirm('Excluir este lançamento e cancelar os alertas pendentes?')) return;
    this.api.excluir(this.id).subscribe({
      next: () => this.router.navigate(['/lancamentos']),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }
}
