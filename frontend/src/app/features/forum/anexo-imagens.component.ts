import { Component, inject, model, signal } from '@angular/core';
import { finalize, forkJoin } from 'rxjs';

import {
  FORUM_MAX_IMAGENS,
  FORUM_TAMANHO_MAX_IMAGEM,
  FORUM_TIPOS_IMAGEM,
  ForumService,
} from '../../core/api/forum.service';
import { urlDaImagem } from '../marketplace/imagem';

/**
 * Seletor de imagens do fórum: envia cada arquivo na hora, mostra a pré-visualização e permite remover.
 * "imagens" guarda as URLs já enviadas (use [(imagens)]); "enviando" fica true enquanto há envio em curso,
 * para o formulário esperar antes de publicar.
 */
@Component({
  selector: 'app-anexo-imagens',
  template: `
    <div class="anexo">
      @if (imagens().length) {
        <ul class="anexo__lista" aria-label="Imagens anexadas">
          @for (url of imagens(); track url) {
            <li>
              <img [src]="urlDaImagem(url)" alt="Imagem anexada" />
              <button type="button" aria-label="Remover imagem" (click)="remover(url)">×</button>
            </li>
          }
        </ul>
      }

      @if (imagens().length < maximo) {
        <label class="anexo__botao">
          <input
            type="file"
            accept="image/png,image/jpeg,image/webp"
            multiple
            hidden
            [disabled]="enviando()"
            (change)="selecionar($event)"
          />
          {{ enviando() ? 'Enviando…' : 'Anexar imagem' }}
        </label>
      }
      <small class="anexo__dica">PNG, JPG ou WEBP, até 5 MB cada, no máximo {{ maximo }} imagens.</small>

      @if (erro(); as mensagem) {
        <p class="anexo__erro" role="alert">{{ mensagem }}</p>
      }
    </div>
  `,
  styles: `
    .anexo {
      display: grid;
      gap: 0.5rem;
      justify-items: start;
      margin: 0.5rem 0;
    }

    .anexo__lista {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
      margin: 0;
      padding: 0;
      list-style: none;
    }

    .anexo__lista li {
      position: relative;
    }

    .anexo__lista img {
      display: block;
      width: 6.5rem;
      height: 6.5rem;
      object-fit: cover;
      border: 1px solid var(--line);
      border-radius: var(--radius);
    }

    .anexo__lista button {
      position: absolute;
      top: 0.25rem;
      right: 0.25rem;
      width: 1.5rem;
      height: 1.5rem;
      padding: 0;
      border-radius: 999px;
      line-height: 1;
    }

    .anexo__botao {
      display: inline-flex;
      align-items: center;
      padding: 0.45rem 0.9rem;
      border: 1px dashed var(--line);
      border-radius: var(--radius);
      cursor: pointer;
    }

    .anexo__dica {
      color: var(--muted);
    }

    .anexo__erro {
      margin: 0;
      color: var(--danger, #ff5d6c);
    }
  `,
})
export class AnexoImagens {
  private readonly forum = inject(ForumService);

  readonly imagens = model<string[]>([]);
  readonly enviando = model(false);

  protected readonly maximo = FORUM_MAX_IMAGENS;
  protected readonly erro = signal<string | null>(null);
  protected readonly urlDaImagem = urlDaImagem;

  protected selecionar(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const escolhidos = Array.from(campo.files ?? []);
    campo.value = '';
    this.erro.set(null);
    if (!escolhidos.length) return;

    const vagas = this.maximo - this.imagens().length;
    const arquivos = escolhidos.slice(0, vagas);
    if (escolhidos.length > vagas) {
      this.erro.set(`Você pode anexar no máximo ${this.maximo} imagens.`);
    }

    for (const arquivo of arquivos) {
      if (!FORUM_TIPOS_IMAGEM.includes(arquivo.type)) {
        this.erro.set(`“${arquivo.name}” não é PNG, JPG ou WEBP.`);
        return;
      }
      if (arquivo.size > FORUM_TAMANHO_MAX_IMAGEM) {
        this.erro.set(`“${arquivo.name}” passa de 5 MB.`);
        return;
      }
    }

    this.enviando.set(true);
    forkJoin(arquivos.map((arquivo) => this.forum.enviarImagem(arquivo)))
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (enviadas) => this.imagens.set([...this.imagens(), ...enviadas.map((e) => e.url)]),
        error: (e: { mensagem?: string }) => this.erro.set(e?.mensagem ?? 'Não foi possível enviar a imagem.'),
      });
  }

  protected remover(url: string): void {
    this.imagens.set(this.imagens().filter((atual) => atual !== url));
  }
}