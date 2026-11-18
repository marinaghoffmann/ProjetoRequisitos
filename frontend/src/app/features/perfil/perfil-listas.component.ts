import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ListaPersonalizada, ListasService } from '../../core/api/listas.service';
import { ErroApi } from '../../core/http/erro-api';

/** Aba "Listas" do perfil (F-04): as listas do usuário logado, com o total de jogos e as primeiras capas. */
@Component({
  selector: 'app-perfil-listas',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="pf-secao">
      <div class="pf-secao__cab">
        <div>
          <h2>Minhas listas</h2>
          <p>{{ listas().length }} {{ listas().length === 1 ? 'lista' : 'listas' }}</p>
        </div>
        <a class="pl-gerenciar" routerLink="/biblioteca/listas">Gerenciar listas</a>
      </div>

      @if (erro(); as mensagem) {
        <p class="pf-aviso" role="alert">{{ mensagem }}</p>
      }

      @if (carregando()) {
        <p class="pf-estado">Carregando…</p>
      } @else {
        <ul class="pl-listas">
          @for (l of listas(); track l.id) {
            <li>
              <a class="pl-lista" [routerLink]="['/biblioteca/listas', l.id]">
                <span class="pl-capas" aria-hidden="true">
                  @for (capa of capas(l); track $index) { <img [src]="capa" alt="" loading="lazy" /> }
                </span>
                <strong>{{ l.titulo }}</strong>
                <small>{{ l.totalJogos }} {{ l.totalJogos === 1 ? 'jogo' : 'jogos' }}</small>
              </a>
            </li>
          } @empty {
            <li class="pf-estado">
              Você ainda não criou listas.
              <a routerLink="/biblioteca/listas">Criar a primeira</a>
            </li>
          }
        </ul>
      }
    </section>
  `,
  styles: `
    /* Mesmo visual das seções do perfil (os estilos de perfil.page.scss não alcançam este componente). */
    :host { display: block; }
    .pf-secao { padding: 28px 0 32px; }
    .pf-secao__cab { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 18px; }
    .pf-secao__cab h2 { margin: 0; font: 700 18px var(--font-title); letter-spacing: -0.02em; }
    .pf-secao__cab p { margin: 4px 0 0; color: var(--muted); font-size: 11px; }
    .pf-estado { padding: 12px 0; color: var(--muted); font-size: 12px; }
    .pf-estado a { margin-left: 6px; font-weight: 700; }
    .pf-aviso { margin-bottom: 14px; padding: 10px 14px; border: 1px solid rgb(255 116 72 / 25%); border-radius: 9px; background: rgb(255 116 72 / 7%); color: #ffc0aa; font-size: 12px; }
    .pl-gerenciar { font-size: 11px; font-weight: 800; }
    .pl-listas { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 14px; margin: 0; padding: 0; list-style: none; }
    .pl-lista { display: grid; gap: 4px; padding: 10px; border: 1px solid var(--line-soft); border-radius: 12px; background: var(--surface); color: var(--text); }
    .pl-lista:hover { border-color: var(--accent); text-decoration: none; }
    .pl-capas { display: grid; grid-template-columns: repeat(4, 1fr); height: 64px; overflow: hidden; border-radius: 8px; background: var(--surface-2); }
    .pl-capas img { width: 100%; height: 100%; object-fit: cover; }
    .pl-lista strong { font-size: 12px; }
    .pl-lista small { color: var(--muted); font-size: 10px; }
  `,
})
export class PerfilListasComponent implements OnInit {
  private readonly servico = inject(ListasService);

  protected readonly listas = signal<ListaPersonalizada[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  ngOnInit(): void {
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

  protected capas(lista: ListaPersonalizada): string[] {
    return lista.itens
      .map((item) => item.jogoCapaUrl)
      .filter((capa): capa is string => !!capa)
      .slice(0, 4);
  }
}
