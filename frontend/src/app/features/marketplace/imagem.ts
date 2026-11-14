/** Só exibe fotos enviadas ao backend ou URLs absolutas; o resto vira o marcador "sem foto". */
export function urlDaImagem(caminho: string | null | undefined): string {
  if (!caminho) return '';
  return caminho.startsWith('/uploads/') || /^https?:\/\//.test(caminho) ? caminho : '';
}
