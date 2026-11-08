import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';

import { CatalogoService, Captura } from '../../core/api/catalogo.service';
import {
  JogandoAgoraService, JogoEmAndamento, LIMITE_JOGANDO_AGORA, Progresso, ROTULOS_PROGRESSO,
} from '../../core/api/jogando-agora.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';
import { EstatisticasJogoComponent } from './estatisticas-jogo.component';

interface Jogo {
  id: string;
  titulo: string;
  resumo?: string | null;
  capaUrl?: string | null;
  dataLancamento?: string | null;
  status?: string;
}

@Component({
  selector: 'app-detalhe-jogo',
  imports: [RouterLink, DatePipe, EstatisticasJogoComponent],
  template: `
    <a class="voltar" routerLink="/catalogo">← Catálogo</a>
    @if (erro()) {
      <p role="alert">{{ erro() }}</p>
    } @else if (jogo(); as j) {
      <section class="topo">
        <div class="capa">
          @if (j.capaUrl) { <img [src]="j.capaUrl" [alt]="'Capa de ' + j.titulo" /> }
        </div>
        <div class="info">
          <h1>{{ j.titulo }}</h1>
          <p class="sub">
            @if (j.dataLancamento) { {{ j.dataLancamento | date: 'dd/MM/yyyy' }} }
          </p>
          <p>{{ j.resumo }}</p>
          <div class="acoes">
            @if (autenticacao.usuarioAtual()) {
              @if (atual(); as a) {
                <select [value]="a.progresso" (change)="alterar(j.id, $any($event.target).value)">
                  @for (p of progressos; track p) {
                    <option [value]="p">{{ rotulos[p] }}</option>
                  }
                </select>
                <button type="button" (click)="remover(j.id)">Parar de jogar</button>
              } @else {
                <button type="button" (click)="marcar(j.id)">Jogando agora</button>
              }
            } @else {
              <a routerLink="/login">Entre para marcar</a>
            }
            <button type="button" disabled>Wishlist</button>
            <button type="button" disabled>Marcar zerado</button>
            <button type="button" disabled>Favoritar</button>
            @if (autenticacao.temPapel('ADMINISTRADOR', 'DESENVOLVEDOR')) {
              <a [routerLink]="['/catalogo', j.id, 'editar']">Editar</a>
            }
          </div>
          @if (erroJogando(); as m) { <p class="erro" role="alert">{{ m }}</p> }
          @if (escolhendoSubstituto()) {
            <div class="lightbox" (click)="escolhendoSubstituto.set(false)">
              <div class="modal" (click)="$event.stopPropagation()">
                <h3>Limite de 5 jogos atingido</h3>
                <p>Escolha qual jogo sai para entrar "{{ j.titulo }}":</p>
                @for (e of emAndamento(); track e.jogoId) {
                  <button type="button" (click)="marcar(j.id, e.jogoId)">{{ e.titulo }}</button>
                }
                <button type="button" (click)="escolhendoSubstituto.set(false)">Cancelar</button>
              </div>
            </div>
          }
        </div>
      </section>

      <nav class="abas">
        <span class="ativa">Visão geral</span><span>Reviews</span><span>Journal</span>
        <span>Listas</span><span>Fórum</span>
      </nav>

      @if (capturas().length) {
        <section class="galeria">
          @for (c of capturas(); track c.completa) {
            <button type="button" (click)="ampliada.set(c.completa)">
              <img [src]="c.miniatura" alt="Captura de tela" loading="lazy" />
            </button>
          }
        </section>
      }
      @if (ampliada(); as url) {
        <div class="lightbox" (click)="ampliada.set(null)">
          <img [src]="url" alt="Captura ampliada" />
        </div>
      }

      <section class="grade">
        <div class="vazio">
          <h2>O que estão dizendo</h2>
          <p>Reviews em breve.</p>
        </div>
        <aside>
          <app-estatisticas-jogo [jogoId]="j.id" />
        </aside>
      </section>
    } @else {
      <p>Carregando…</p>
    }
  `,
  styleUrls: ['./detalhe-jogo.page.scss'],
})
export class DetalheJogoPage {
  protected readonly autenticacao = inject(AutenticacaoService);
  private readonly catalogo = inject(CatalogoService);
  private readonly jogando = inject(JogandoAgoraService);

  protected readonly jogo = signal<Jogo | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly capturas = signal<Captura[]>([]);
  protected readonly ampliada = signal<string | null>(null);

  protected readonly emAndamento = signal<JogoEmAndamento[]>([]);
  protected readonly escolhendoSubstituto = signal(false);
  protected readonly erroJogando = signal<string | null>(null);
  protected readonly progressos = Object.keys(ROTULOS_PROGRESSO) as Progresso[];
  protected readonly rotulos = ROTULOS_PROGRESSO;
  protected readonly atual = computed(
    () => this.emAndamento().find((e) => e.jogoId === this.jogo()?.id) ?? null,
  );

  constructor() {
    const id = inject(ActivatedRoute).snapshot.paramMap.get('id');
    inject(HttpClient)
      .get<Jogo>(`/api/catalogo/jogos/${id}`)
      .subscribe({
        next: (j) => {
          this.jogo.set(j);
          this.catalogo.capturas(j.id).subscribe({ next: (c) => this.capturas.set(c), error: () => {} });
          if (this.autenticacao.usuarioAtual()) this.recarregar();
        },
        error: () => this.erro.set('Jogo não encontrado.'),
      });
  }

  private recarregar(): void {
    this.jogando.listar().subscribe({ next: (l) => this.emAndamento.set(l) });
  }

  protected marcar(id: string, substituir?: string): void {
    if (!substituir && this.emAndamento().length >= LIMITE_JOGANDO_AGORA) {
      this.escolhendoSubstituto.set(true);
      return;
    }
    this.erroJogando.set(null);
    this.jogando.marcar(id, substituir).subscribe({
      next: () => { this.escolhendoSubstituto.set(false); this.recarregar(); },
      error: (e: ErroApi) => this.erroJogando.set(e.mensagem),
    });
  }

  protected alterar(id: string, progresso: Progresso): void {
    this.jogando.alterarProgresso(id, progresso).subscribe({
      next: () => this.recarregar(),
      error: (e: ErroApi) => this.erroJogando.set(e.mensagem),
    });
  }

  protected remover(id: string): void {
    this.jogando.remover(id).subscribe({ next: () => this.recarregar() });
  }
}