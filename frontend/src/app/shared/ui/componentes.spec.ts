import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { gradientePara, juntarMeta, plataformasDe } from '../apresentacao-jogo';
import { CabecalhoPaginaComponent } from './cabecalho-pagina.component';
import { CardPosterComponent, DadosPoster } from './card-poster.component';
import { ChipsComponent } from './chips.component';

const POSTER: DadosPoster = {
  id: 'jogo-1', titulo: 'Hollow Knight', capa: null, gradiente: 'linear-gradient(red, blue)',
  generos: ['Metroidvania'], ano: '2017', plataformas: ['PC', 'Switch'], nota: 4.25, naWishlist: false,
};

describe('componentes compartilhados', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [provideRouter([])] }));

  it('cabeçalho mostra eyebrow, título e subtítulo', () => {
    const f = TestBed.createComponent(CabecalhoPaginaComponent);
    f.componentRef.setInput('titulo', 'Todos os jogos');
    f.componentRef.setInput('eyebrow', 'Catálogo');
    f.componentRef.setInput('subtitulo', '3 jogos');
    f.detectChanges();
    const el: HTMLElement = f.nativeElement;
    expect(el.querySelector('h1')?.textContent).toBe('Todos os jogos');
    expect(el.querySelector('.cp__eyebrow')?.textContent).toBe('Catálogo');
    expect(el.querySelector('.cp__subtitulo')?.textContent).toBe('3 jogos');
  });

  it('cabeçalho sem eyebrow não desenha o traço', () => {
    const f = TestBed.createComponent(CabecalhoPaginaComponent);
    f.componentRef.setInput('titulo', 'Wishlist');
    f.detectChanges();
    expect(f.nativeElement.querySelector('.cp__eyebrow')).toBeNull();
  });

  it('chip ativo fica marcado e o clique troca a seleção na hora', () => {
    const f = TestBed.createComponent(ChipsComponent<string>);
    f.componentRef.setInput('opcoes', [{ valor: '', rotulo: 'Todos' }, { valor: 'RPG', rotulo: 'RPG' }]);
    f.componentRef.setInput('selecionado', '');
    f.detectChanges();
    const botoes: HTMLButtonElement[] = [...f.nativeElement.querySelectorAll('button')];
    expect(botoes[0].classList).toContain('chip--ativo');
    botoes[1].click();
    f.detectChanges();
    expect(f.componentInstance.selecionado()).toBe('RPG');
    expect(botoes[1].getAttribute('aria-pressed')).toBe('true');
  });

  it('pôster mostra gênero, nota, "ano · plataforma" e não mostra resumo', () => {
    const f = TestBed.createComponent(CardPosterComponent);
    f.componentRef.setInput('jogo', POSTER);
    f.detectChanges();
    const el: HTMLElement = f.nativeElement;
    expect(el.querySelector('.poster__genero')?.textContent).toBe('Metroidvania');
    expect(el.querySelector('.poster__nota')?.textContent?.trim()).toBe('4,3');
    expect(el.querySelector('.poster__meta')?.textContent).toBe('2017 · PC');
    expect(el.querySelector('img')).toBeNull();
  });

  it('pôster sem nota pública mostra "—" e o coração emite a troca da wishlist', () => {
    const f = TestBed.createComponent(CardPosterComponent);
    f.componentRef.setInput('jogo', { ...POSTER, nota: null });
    const emitidos: DadosPoster[] = [];
    f.componentInstance.alternarWishlist.subscribe((j) => emitidos.push(j));
    f.detectChanges();
    expect(f.nativeElement.querySelector('.poster__nota')?.textContent?.trim()).toBe('—');
    (f.nativeElement.querySelector('.poster__coracao') as HTMLButtonElement).click();
    expect(emitidos.map((j) => j.id)).toEqual(['jogo-1']);
  });
});

describe('apresentação do jogo', () => {
  it('lê as plataformas dos metadados e ignora JSON inválido', () => {
    expect(plataformasDe({ metadadosJson: '{"plataformas":["PC"," PS5 "]}' })).toEqual(['PC', 'PS5']);
    expect(plataformasDe({ metadadosJson: 'quebrado' })).toEqual([]);
  });

  it('junta só as partes preenchidas e mantém o gradiente estável por jogo', () => {
    expect(juntarMeta('2017', null, '', 'PC')).toBe('2017 · PC');
    expect(gradientePara('abc')).toBe(gradientePara('abc'));
  });
});
