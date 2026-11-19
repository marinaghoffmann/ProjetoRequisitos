import { JogoNaVitrine } from '../../core/catalogo/vitrine.service';

export type Ordenacao = 'MELHOR_AVALIADOS' | 'MAIS_AVALIADOS' | 'RECENTES' | 'TITULO';

export const OPCOES_DE_ORDENACAO: { valor: Ordenacao; rotulo: string }[] = [
  { valor: 'MELHOR_AVALIADOS', rotulo: 'Melhor avaliados ↓' },
  { valor: 'MAIS_AVALIADOS', rotulo: 'Mais avaliados ↓' },
  { valor: 'RECENTES', rotulo: 'Lançamento ↓' },
  { valor: 'TITULO', rotulo: 'Título A–Z' },
];

/** Jogos sem nota pública (menos de 3 reviews, F-17 RN 3) ficam depois dos que têm. */
export function ordenar(jogos: JogoNaVitrine[], ordem: Ordenacao): JogoNaVitrine[] {
  const porTitulo = (a: JogoNaVitrine, b: JogoNaVitrine) => a.titulo.localeCompare(b.titulo, 'pt-BR');
  const copia = [...jogos];
  switch (ordem) {
    case 'MELHOR_AVALIADOS':
      return copia.sort((a, b) => (b.nota ?? -1) - (a.nota ?? -1) || b.avaliacoes - a.avaliacoes || porTitulo(a, b));
    case 'MAIS_AVALIADOS':
      return copia.sort((a, b) => b.avaliacoes - a.avaliacoes || porTitulo(a, b));
    case 'RECENTES':
      return copia.sort((a, b) => (b.jogo.dataLancamento ?? '').localeCompare(a.jogo.dataLancamento ?? '') || porTitulo(a, b));
    case 'TITULO':
      return copia.sort(porTitulo);
  }
}
