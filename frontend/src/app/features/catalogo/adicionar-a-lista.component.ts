import { Component, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { LIMITE_NOTA_ITEM, ListaPersonalizada, ListasService } from '../../core/api/listas.service';
import { ErroApi } from '../../core/http/erro-api';

/**
 * Botão "Adicionar à lista" da página do jogo (F-04): escolhe uma das listas do usuário logado e, se
 * quiser, escreve a nota explicativa. Listas que já têm o jogo (RN 2) ou estão cheias (RN 3) aparecem
 * desabilitadas; a API confere as duas regras de novo.
 */
@Component({
  selector: 'app-adicionar-a-lista',
  imports: [RouterLink],
  template: `
    <button type="button" class="abrir" (click)="abrir()">Adicionar à lista</button>

    @if (aberto()) {
      <div class="fundo" (click)="aberto.set(false)"></div>
      <div class="dialogo" role="dialog" aria-modal="true" aria-labelledby="titulo-adicionar-lista">
        <header>
          <h2 id="titulo-adicionar-lista">Adicionar à lista</h2>
          <button type="button" class="fechar" aria-label="Fechar" (click)="aberto.set(false)">×</button>
        </header>

        @if (carregando()) {
          <p class="detalhe">Carregando suas listas…</p>
        } @else if (!listas().length) {
          <p class="detalhe">Você ainda não tem listas. <a routerLink="/biblioteca/listas">Criar uma lista</a></p>
        } @else {
          <form (submit)="$event.preventDefault(); adicionar()">
            <label>
              Lista *
              <select [value]="listaId()" (change)="listaId.set($any($event.target).value)">
                <option value="">Escolha uma lista</option>
                @for (l of listas(); track l.id) {
                  <option [value]="l.id" [disabled]="indisponivel(l)">
                    {{ l.titulo }} ({{ l.totalJogos }}/{{ l.limiteDeJogos }}){{ contem(l) ? ' · já está nesta lista' : '' }}
                  </option>
                }
              </select>
            </label>
            <label>
              Nota explicativa (opcional)
              <textarea rows="3" [maxLength]="limiteNota" [value]="nota()" (input)="nota.set($any($event.target).value)"
                placeholder="Por que este jogo está na lista?"></textarea>
            </label>
            @if (erro(); as mensagem) {
              <p class="mensagem-erro" role="alert">{{ mensagem }}</p>
            }
            <button type="submit" [disabled]="!listaId() || salvando()">Adicionar</button>
            <a class="detalhe" routerLink="/biblioteca/listas">Gerenciar minhas listas</a>
          </form>
        }
      </div>
    }
    @if (sucesso(); as mensagem) {
      <p class="sucesso" role="status">{{ mensagem }}</p>
    }
  `,
  styles: `
    :host { display: contents; }
    .abrir { min-height: auto; padding: 8px 14px; border: 1px solid #333; border-radius: 8px; background: #14141a; color: inherit; font-size: 13px; font-weight: 400; }
    .sucesso { flex-basis: 100%; margin: 0; padding: 0.4rem 0.75rem; font-size: 0.8rem; }
    .fundo { position: fixed; inset: 0; z-index: 40; background: rgb(0 0 0 / 60%); }
    .dialogo {
      position: fixed; top: 50%; left: 50%; z-index: 41; width: min(30rem, calc(100% - 2rem));
      max-height: calc(100vh - 2rem); overflow-y: auto; padding: 1.5rem; border: 1px solid var(--line);
      border-radius: var(--radius); background: var(--surface); box-shadow: var(--shadow); transform: translate(-50%, -50%);
    }
    .dialogo header { display: flex; align-items: center; justify-content: space-between; }
    .dialogo h2 { margin: 0; font-size: 1.3rem; }
    .dialogo form { margin-top: 1rem; }
    .fechar { min-height: 2rem; padding: 0 0.6rem; background: transparent; color: var(--muted); font-size: 1.3rem; }
    .detalhe { color: var(--muted); font-size: 0.8rem; }
  `,
})
export class AdicionarAListaComponent {
  private readonly servico = inject(ListasService);

  readonly jogoId = input.required<string>();

  protected readonly limiteNota = LIMITE_NOTA_ITEM;
  protected readonly aberto = signal(false);
  protected readonly carregando = signal(false);
  protected readonly listas = signal<ListaPersonalizada[]>([]);
  protected readonly listaId = signal('');
  protected readonly nota = signal('');
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly sucesso = signal<string | null>(null);

  protected abrir(): void {
    this.aberto.set(true);
    this.carregando.set(true);
    this.erro.set(null);
    this.sucesso.set(null);
    this.listaId.set('');
    this.nota.set('');
    this.servico.minhas().subscribe({
      next: (listas) => {
        this.listas.set(listas);
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }

  protected contem(lista: ListaPersonalizada): boolean {
    return lista.itens.some((item) => item.jogoId === this.jogoId());
  }

  protected indisponivel(lista: ListaPersonalizada): boolean {
    return this.contem(lista) || lista.totalJogos >= lista.limiteDeJogos;
  }

  protected adicionar(): void {
    this.salvando.set(true);
    this.erro.set(null);
    this.servico.adicionarJogo(this.listaId(), this.jogoId(), this.nota()).subscribe({
      next: (lista) => {
        this.salvando.set(false);
        this.aberto.set(false);
        this.sucesso.set(`Adicionado à lista "${lista.titulo}" (${lista.totalJogos}/${lista.limiteDeJogos}).`);
      },
      error: (e: ErroApi) => {
        this.salvando.set(false);
        this.erro.set(e.mensagem);
      },
    });
  }
}
