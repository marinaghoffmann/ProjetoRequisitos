import { Jogo } from '../core/api/catalogo.service';

/** Gradientes da Início, usados como capa quando o jogo não tem imagem. */
export const GRADIENTES_DE_CAPA = [
  'linear-gradient(160deg, #1b1445, #0f2b4a 55%, #3b1d5c)',
  'linear-gradient(170deg, #5a6a7a, #a45a3a 60%, #3a2418)',
  'linear-gradient(170deg, #cdbba6, #8a6f5a 55%, #3b3a3f)',
  'linear-gradient(170deg, #1b1230, #14614a 55%, #0b1a14)',
  'linear-gradient(170deg, #e59a82, #3f7a8a 60%, #1c3a3a)',
  'linear-gradient(170deg, #e8c9a8, #6a7a6a 60%, #2d3a36)',
];

/** Sempre o mesmo gradiente para o mesmo jogo, para a capa não "piscar" entre telas. */
export function gradientePara(id: string | null | undefined): string {
  if (!id) return GRADIENTES_DE_CAPA[0];
  let soma = 0;
  for (const c of id) soma = (soma * 31 + c.charCodeAt(0)) >>> 0;
  return GRADIENTES_DE_CAPA[soma % GRADIENTES_DE_CAPA.length];
}

/** Só aceita imagens do próprio backend (/uploads/) ou http(s); o resto vira "sem capa". */
export function imagemSegura(caminho: string | null | undefined): string | null {
  if (!caminho) return null;
  if (caminho.startsWith('/uploads/') || caminho.startsWith('http://') || caminho.startsWith('https://')) {
    return caminho;
  }
  return null;
}

export function anoDe(data: string | null | undefined): string | null {
  return data && /^\d{4}/.test(data) ? data.slice(0, 4) : null;
}

/** Plataformas informadas no cadastro (metadados.plataformas). Ausentes = lista vazia. */
export function plataformasDe(jogo: Pick<Jogo, 'metadadosJson'>): string[] {
  try {
    const meta = JSON.parse(jogo.metadadosJson || '{}') as Record<string, unknown>;
    const valor = meta['plataformas'];
    if (Array.isArray(valor)) return valor.map(String).map((p) => p.trim()).filter(Boolean);
    if (typeof valor === 'string') return valor.split(',').map((p) => p.trim()).filter(Boolean);
  } catch {
    // metadados inválidos: sem plataformas
  }
  return [];
}

/** Junta as partes que existem com " · ", omitindo as vazias. */
export function juntarMeta(...partes: (string | number | null | undefined)[]): string {
  return partes.filter((p) => p !== null && p !== undefined && `${p}`.trim() !== '').join(' · ');
}

/** Nota com uma casa, no formato brasileiro (4,3). */
export function formatarNota(nota: number | null | undefined): string {
  return nota === null || nota === undefined ? '—' : nota.toLocaleString('pt-BR', {
    minimumFractionDigits: 1, maximumFractionDigits: 1,
  });
}
