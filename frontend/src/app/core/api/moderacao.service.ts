import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export type TipoConteudo = 'TOPICO' | 'RESPOSTA' | 'REVIEW' | 'LISTA' | 'AVALIACAO_MARKETPLACE' | 'ANUNCIO' | 'PERFIL' | 'JOURNAL';
export type AcaoModeracao = 'OCULTAR' | 'EXCLUIR' | 'EDITAR' | 'MOVER' | 'ENCERRAR_TOPICO' | 'MARCAR_18' | 'RETIRAR_18' | 'REVERTER_OCULTACAO' | 'REVERTER_EXCLUSAO';
export interface EstadoModeracao { aviso18: boolean; oculto: boolean; removido: boolean; }
export interface DenunciaModeracao {
  id: string; denuncianteId: string; tipo: TipoConteudo; conteudoId: string; motivo: string;
  status: 'PENDENTE' | 'PROCEDENTE' | 'IMPROCEDENTE'; registradaEm: string; analisadaEm: string | null;
}
export interface LogModeracao {
  id: string; moderadorId: string; acao: string; tipo: TipoConteudo; conteudoId: string;
  justificativa: string | null; registradoEm: string;
}
export interface AdvertenciaModeracao { id: string; usuarioId: string; moderadorId: string; motivo: string; registradaEm: string; }
export interface PaginaSpring<T> { content: T[]; totalElements: number; totalPages: number; number: number; }
export interface PreviaConteudo { autorId: string; topicoId: string; previa: string | null; }

@Injectable({ providedIn: 'root' })
export class ModeracaoService {
  private readonly http = inject(HttpClient);
  estado(tipo: TipoConteudo, id: string) { return this.http.get<EstadoModeracao>(`/api/moderacao/conteudos/${tipo}/${id}/estado`); }
  denunciar(tipoConteudo: TipoConteudo, conteudoId: string, motivo: string) {
    return this.http.post<DenunciaModeracao>('/api/denuncias', { tipoConteudo, conteudoId, motivo });
  }
  fila(pagina = 1, tamanho = 10) {
    return this.http.get<PaginaSpring<DenunciaModeracao>>('/api/moderacao/denuncias', { params: new HttpParams().set('pagina', pagina).set('tamanho', tamanho) });
  }
  logs(pagina = 1, tamanho = 10) {
    return this.http.get<PaginaSpring<LogModeracao>>('/api/moderacao/logs', { params: new HttpParams().set('pagina', pagina).set('tamanho', tamanho) });
  }
  previa(tipo: TipoConteudo, id: string) { return this.http.get<PreviaConteudo>(`/api/moderacao/conteudos/${tipo}/${id}/previa`); }
  agir(acao: AcaoModeracao, tipoConteudo: TipoConteudo, conteudoId: string, justificativa: string,
    extras: { novoTitulo?: string; novoConteudo?: string; novaCategoria?: string; novaRespostaPaiId?: string | null } = {}) {
    return this.http.post<LogModeracao>('/api/moderacao/acoes', { acao, tipoConteudo, conteudoId, justificativa, ...extras });
  }
  arquivar(id: string, justificativa: string) { return this.http.post<LogModeracao>(`/api/moderacao/denuncias/${id}/arquivar`, { justificativa }); }
  advertir(usuarioId: string, motivo: string) { return this.http.post<AdvertenciaModeracao>('/api/moderacao/advertencias', { usuarioId, motivo }); }
  totalAdvertencias(usuarioId: string) { return this.http.get<{ total: number }>(`/api/moderacao/usuarios/${usuarioId}/advertencias/total`); }
}
