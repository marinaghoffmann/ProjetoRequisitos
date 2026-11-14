import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { PerfilVendedor, ReputacaoService, TAGS_COMPORTAMENTO } from '../../core/api/reputacao.service';
import { ErroApi } from '../../core/http/erro-api';

/** Perfil público de vendedor (F-21): reputação e avaliações recebidas. */
@Component({
  selector: 'app-perfil-vendedor',
  imports: [DatePipe, DecimalPipe, RouterLink],
  templateUrl: './perfil-vendedor.page.html',
  styleUrl: './perfil-vendedor.page.scss',
})
export class PerfilVendedorPage {
  private readonly reputacao = inject(ReputacaoService);

  protected readonly tags = TAGS_COMPORTAMENTO;
  protected readonly escala = [1, 2, 3, 4, 5];
  protected readonly perfil = signal<PerfilVendedor | null>(null);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    const id = inject(ActivatedRoute).snapshot.paramMap.get('id') ?? '';
    this.reputacao.perfil(id).subscribe({
      next: (perfil) => this.perfil.set(perfil),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }
}
