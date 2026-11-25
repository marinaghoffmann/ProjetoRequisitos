import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { CatalogoService, Desenvolvedora, Genero } from '../../core/api/catalogo.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-cadastros-base',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cadastros-base.page.html',
  styleUrl: './cadastro-jogo.page.scss',
})
export class CadastrosBasePage {
  private readonly catalogo = inject(CatalogoService);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly desenvolvedoras = signal<Desenvolvedora[]>([]);
  protected readonly generos = signal<Genero[]>([]);
  protected readonly carregando = signal(true);
  protected readonly salvandoDev = signal(false);
  protected readonly salvandoGenero = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly sucesso = signal<string | null>(null);

  protected readonly formDev = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(160)]],
    biografia: ['', Validators.maxLength(2000)],
    site: ['', Validators.maxLength(500)],
    redesSociais: ['', Validators.maxLength(1000)],
    pais: ['', Validators.maxLength(120)],
  });

  protected readonly formGenero = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(80)]],
  });

  constructor() {
    this.carregar();
  }

  private carregar(): void {
    forkJoin({
      desenvolvedoras: this.catalogo.listarDesenvolvedoras(),
      generos: this.catalogo.listarGeneros(),
    }).subscribe({
      next: (d) => {
        this.desenvolvedoras.set(d.desenvolvedoras);
        this.generos.set(d.generos);
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }

  protected cadastrarDesenvolvedora(): void {
    if (this.formDev.invalid || this.salvandoDev()) {
      this.formDev.markAllAsTouched();
      return;
    }
    this.salvandoDev.set(true);
    this.erro.set(null);
    this.sucesso.set(null);
    this.catalogo.cadastrarDesenvolvedora(this.formDev.getRawValue()).subscribe({
      next: (d) => {
        this.salvandoDev.set(false);
        this.sucesso.set(`Desenvolvedora "${d.nome}" cadastrada.`);
        this.formDev.reset();
        this.carregar();
      },
      error: (e: ErroApi) => {
        this.salvandoDev.set(false);
        this.erro.set(e.mensagem);
      },
    });
  }

  protected desativar(d: Desenvolvedora): void {
    if (!confirm(`Desativar "${d.nome}"? Os jogos dela continuam no catálogo.`)) return;
    this.erro.set(null);
    this.sucesso.set(null);
    this.catalogo.excluirDesenvolvedora(d.id).subscribe({
      next: () => {
        this.sucesso.set(`"${d.nome}" foi desativada.`);
        this.carregar();
      },
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  protected cadastrarGenero(): void {
    if (this.formGenero.invalid || this.salvandoGenero()) {
      this.formGenero.markAllAsTouched();
      return;
    }
    this.salvandoGenero.set(true);
    this.erro.set(null);
    this.sucesso.set(null);
    this.catalogo.cadastrarGenero(this.formGenero.getRawValue().nome.trim()).subscribe({
      next: (g) => {
        this.salvandoGenero.set(false);
        this.sucesso.set(`Gênero "${g.nome}" disponível no catálogo.`);
        this.formGenero.reset();
        this.carregar();
      },
      error: (e: ErroApi) => {
        this.salvandoGenero.set(false);
        this.erro.set(e.mensagem);
      },
    });
  }

  protected excluirGenero(g: Genero): void {
    if (!confirm(`Excluir o gênero "${g.nome}"? Essa ação não pode ser desfeita.`)) return;
    this.erro.set(null);
    this.sucesso.set(null);
    this.catalogo.excluirGenero(g.id).subscribe({
      next: () => {
        this.sucesso.set(`Gênero "${g.nome}" excluído.`);
        this.carregar();
      },
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }
}