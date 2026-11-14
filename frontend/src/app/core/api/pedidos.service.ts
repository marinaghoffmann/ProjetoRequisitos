import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export type OpcaoFrete = 'ENVIO_SEGURO' | 'EXPRESSO';
export type FormaPagamento = 'CARTAO' | 'PIX' | 'SALDO_CARTEIRA';
export type StatusPedido = 'AGUARDANDO_ENVIO' | 'ENVIADO' | 'CONCLUIDO' | 'CANCELADO';
export type StatusPagamento = 'EM_CUSTODIA' | 'LIBERADO' | 'REEMBOLSADO';

export const FRETES: Record<OpcaoFrete, string> = {
  ENVIO_SEGURO: 'Envio seguro',
  EXPRESSO: 'Expresso',
};

export const FORMAS_PAGAMENTO: Record<FormaPagamento, string> = {
  CARTAO: 'Cartão (simulação)',
  PIX: 'PIX',
  SALDO_CARTEIRA: 'Saldo da carteira',
};

export const STATUS_PEDIDO: Record<StatusPedido, string> = {
  AGUARDANDO_ENVIO: 'Aguardando envio',
  ENVIADO: 'Enviado',
  CONCLUIDO: 'Concluído',
  CANCELADO: 'Cancelado',
};

export const STATUS_PAGAMENTO: Record<StatusPagamento, string> = {
  EM_CUSTODIA: 'Em custódia',
  LIBERADO: 'Liberado',
  REEMBOLSADO: 'Reembolsado',
};

export interface EnderecoEntrega {
  destinatario: string;
  cep: string;
  rua: string;
  numero: string;
  complemento: string | null;
  cidade: string;
  uf: string;
}

export interface CartaoSimulado {
  numero: string;
  nomeImpresso: string;
  validade: string;
  cvv: string;
}

export interface ResumoCheckout {
  origem: 'PROPOSTA' | 'COMPRA_DIRETA';
  anuncioId: string;
  propostaId: string | null;
  tituloAnuncio: string;
  fotoAnuncio: string | null;
  valorItem: number;
  /** Proposta de troca: o item sai por R$ 0,00 e o comprador paga só o frete. */
  jogoOferecidoEmTroca: string | null;
  prazoParaPagar: string | null;
  saldoCarteira: number;
  fretes: { opcao: OpcaoFrete; valor: number }[];
}

export interface NovoPedido {
  anuncioId: string | null;
  propostaId: string | null;
  endereco: EnderecoEntrega;
  frete: OpcaoFrete;
  formaPagamento: FormaPagamento;
  cartao: CartaoSimulado | null;
}

export interface Pedido {
  id: string;
  /** Do ponto de vista de quem consulta. */
  papel: 'COMPRA' | 'VENDA';
  origem: 'PROPOSTA' | 'COMPRA_DIRETA';
  anuncioId: string;
  tituloAnuncio: string | null;
  propostaId: string | null;
  jogoOferecidoEmTroca: string | null;
  compradorId: string;
  vendedorId: string;
  outraParteId: string;
  nomeOutraParte: string | null;
  valorItem: number;
  frete: OpcaoFrete;
  valorFrete: number;
  total: number;
  endereco: EnderecoEntrega;
  formaPagamento: FormaPagamento;
  finalCartao: string | null;
  statusPagamento: StatusPagamento;
  status: StatusPedido;
  codigoRastreio: string | null;
  criadoEm: string;
  prazoEnvio: string;
  enviadoEm: string | null;
  /** RN 7: quando a custódia é liberada sozinha, se o comprador não confirmar antes. */
  liberacaoAutomaticaEm: string | null;
  concluidoEm: string | null;
  canceladoEm: string | null;
}

/** F-20: checkout, pedidos e carteira. */
@Injectable({ providedIn: 'root' })
export class PedidosService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/marketplace';

  /** Prévia sem cobrança: confere a origem do pedido e traz fretes e saldo da carteira. */
  checkout(origem: { anuncioId?: string | null; propostaId?: string | null }): Observable<ResumoCheckout> {
    let params = new HttpParams();
    if (origem.anuncioId) params = params.set('anuncioId', origem.anuncioId);
    if (origem.propostaId) params = params.set('propostaId', origem.propostaId);
    return this.http.get<ResumoCheckout>(`${this.base}/checkout`, { params });
  }

  gerar(pedido: NovoPedido): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.base}/pedidos`, pedido);
  }

  listar(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.base}/pedidos`);
  }

  registrarEnvio(id: string, codigoRastreio: string): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.base}/pedidos/${id}/envio`, { codigoRastreio });
  }

  confirmarRecebimento(id: string): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.base}/pedidos/${id}/recebimento`, null);
  }

  saldo(): Observable<{ saldo: number }> {
    return this.http.get<{ saldo: number }>(`${this.base}/carteira`);
  }
}
