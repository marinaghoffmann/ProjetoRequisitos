import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ConteudoModeravelComponent } from './conteudo-moderavel.component';

describe('aviso 18+ por item', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [ConteudoModeravelComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  it('desfoca até o visitante revelar e guarda a escolha na sessão', () => {
    const fixture = TestBed.createComponent(ConteudoModeravelComponent);
    fixture.componentRef.setInput('tipo', 'REVIEW');
    fixture.componentRef.setInput('conteudoId', 'review-1');
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/moderacao/conteudos/REVIEW/review-1/estado')
      .flush({ aviso18: true, oculto: false, removido: false });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.corpo.desfocado')).toBeTruthy();
    const revelar = [...fixture.nativeElement.querySelectorAll('button')]
      .find((botao: HTMLButtonElement) => botao.textContent?.includes('Revelar conteúdo')) as HTMLButtonElement;
    revelar.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.corpo.desfocado')).toBeNull();
    expect(sessionStorage.getItem('game-tracker.revelado.REVIEW.review-1')).toBe('1');
    http.verify();
  });
});
