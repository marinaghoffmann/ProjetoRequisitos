import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export type StatusLancamento = 'FUTURO_LANCAMENTO' | 'LANCADO';
export type TipoDataLancamento = 'CONFIRMADA' | 'ESTIMADA';
export interface AlteracaoCronograma {
  id: string;
  dataAnterior: string;
  novaData: string;
  alteradoPorId: string;
  alteradoEm: string;
}
export interface Lancamento {
  id: string;
  jogoId: string;
  desenvolvedoraId: string;
  titulo: string;
  capaUrl: string;
  dataPrevista: string;
  status: StatusLancamento;
  tipoData: TipoDataLancamento;
  diasRestantes: number;
  totalInteressados: number;
  alertaAtivoDoUsuario: boolean;
  historico: AlteracaoCronograma[];
}
@Injectable({ providedIn: 'root' })
export class LancamentosService {
  private readonly http = inject(HttpClient);
  listar(incluirLancados = false): Observable<Lancamento[]> {
    const url = incluirLancados ? '/api/lancamentos?incluirLancados=true' : '/api/lancamentos';
    return this.http.get<Lancamento[]>(url);
  }
  buscar(id: string): Observable<Lancamento> { return this.http.get<Lancamento>(`/api/lancamentos/${id}`); }
  cadastrar(jogoId: string, dataPrevista: string, tipoData: TipoDataLancamento): Observable<Lancamento> {
    return this.http.post<Lancamento>('/api/lancamentos', { jogoId, dataPrevista, tipoData });
  }
  alterar(id: string, dataPrevista: string, tipoData: TipoDataLancamento): Observable<Lancamento> {
    return this.http.put<Lancamento>(`/api/lancamentos/${id}`, { dataPrevista, tipoData });
  }
  excluir(id: string): Observable<void> { return this.http.delete<void>(`/api/lancamentos/${id}`); }
  marcar(id: string): Observable<void> { return this.http.put<void>(`/api/lancamentos/${id}/interesse`, {}); }
  desmarcar(id: string): Observable<void> { return this.http.delete<void>(`/api/lancamentos/${id}/interesse`); }
}
