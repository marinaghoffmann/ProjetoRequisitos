import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { StatusAnuncio, TipoNegociacao } from './marketplace.service';

export type TipoProposta = 'VALOR' | 'TROCA';
export type StatusProposta = 'PENDENTE' | 'ACEITA' | 'RECUSADA' | 'CANCELADA' | 'EXPIRADA';
export type PapelNaConversa = 'COMPRADOR' | 'VENDEDOR';

export const STATUS_PROPOSTA: Record<StatusProposta, string> = {
  PENDENTE: 'Pendente',
  ACEITA: 'Aceita',
  RECUSADA: 'Recusada',
  CANCELADA: 'Cancelada',
  EXPIRADA: 'Expirada',
};

export interface MensagemChat {
  id: string;
  remetenteId: string;
  texto: string;
  enviadaEm: string;
  lidaEm: string | null;
}

export interface PropostaNegociacao {
  id: string;
  tipo: TipoProposta;
  valorOferecido: number | null;
  jogoOferecido: string | null;
  status: StatusProposta;
  /** Aviso da recusa automática ("item reservado para outro comprador") ou da expiração. */
  motivo: string | null;
  pedidoId: string | null;
  enviadaEm: string;
  respondidaEm: string | null;
  /** Só na proposta aceita: até quando o comprador pode gerar o pedido (RN 10). */
  prazoParaPagar: string | null;
}

export interface ResumoConversa {
  id: string;
  anuncioId: string;
  tituloAnuncio: string | null;
  fotoAnuncio: string | null;
  statusAnuncio: StatusAnuncio | null;
  papel: PapelNaConversa;
  outraParteId: string;
  nomeOutraParte: string | null;
  ultimaMensagem: string | null;
  statusUltimaProposta: StatusProposta | null;
  naoLidas: number;
  ultimaAtividadeEm: string;
}

export interface Conversa {
  id: string;
  anuncioId: string;
  tituloAnuncio: string | null;
  fotoAnuncio: string | null;
  precoAnuncio: number | null;
  tipoNegociacao: TipoNegociacao | null;
  statusAnuncio: StatusAnuncio | null;
  compradorId: string;
  nomeComprador: string | null;
  vendedorId: string;
  nomeVendedor: string | null;
  /** RN 5: anúncio vendido ou cancelado. */
  somenteLeitura: boolean;
  naoLidas: number;
  iniciadaEm: string;
  ultimaAtividadeEm: string;
  mensagens: MensagemChat[];
  propostas: PropostaNegociacao[];
}

export interface NovaProposta {
  tipo: TipoProposta;
  valor: number | null;
  jogoOferecido: string | null;
  mensagem: string | null;
}

/** F-19: conversas, mensagens e propostas entre comprador e vendedor. */
@Injectable({ providedIn: 'root' })
export class NegociacaoService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/marketplace';

  /** RN 1 e RN 2: abre a conversa do comprador com o vendedor, ou reabre a que já existe. */
  iniciar(anuncioId: string): Observable<Conversa> {
    return this.http.post<Conversa>(`${this.base}/anuncios/${anuncioId}/conversas`, null);
  }

  listar(): Observable<ResumoConversa[]> {
    return this.http.get<ResumoConversa[]>(`${this.base}/conversas`);
  }

  naoLidas(): Observable<{ total: number }> {
    return this.http.get<{ total: number }>(`${this.base}/conversas/nao-lidas`);
  }

  conversa(id: string): Observable<Conversa> {
    return this.http.get<Conversa>(`${this.base}/conversas/${id}`);
  }

  marcarComoLidas(id: string): Observable<void> {
    return this.http.post<void>(`${this.base}/conversas/${id}/leitura`, null);
  }

  enviarMensagem(id: string, texto: string): Observable<MensagemChat> {
    return this.http.post<MensagemChat>(`${this.base}/conversas/${id}/mensagens`, { texto });
  }

  enviarProposta(id: string, proposta: NovaProposta): Observable<PropostaNegociacao> {
    return this.http.post<PropostaNegociacao>(`${this.base}/conversas/${id}/propostas`, proposta);
  }

  aceitar(id: string, propostaId: string): Observable<PropostaNegociacao> {
    return this.http.post<PropostaNegociacao>(`${this.base}/conversas/${id}/propostas/${propostaId}/aceite`, null);
  }

  recusar(id: string, propostaId: string): Observable<PropostaNegociacao> {
    return this.http.post<PropostaNegociacao>(`${this.base}/conversas/${id}/propostas/${propostaId}/recusa`, null);
  }

  cancelar(id: string, propostaId: string): Observable<PropostaNegociacao> {
    return this.http.post<PropostaNegociacao>(
      `${this.base}/conversas/${id}/propostas/${propostaId}/cancelamento`,
      null,
    );
  }

  /** RN 10: o vendedor libera a reserva antes do prazo de 48 horas. */
  liberarReserva(id: string, propostaId: string): Observable<PropostaNegociacao> {
    return this.http.post<PropostaNegociacao>(`${this.base}/conversas/${id}/propostas/${propostaId}/liberacao`, null);
  }
}
