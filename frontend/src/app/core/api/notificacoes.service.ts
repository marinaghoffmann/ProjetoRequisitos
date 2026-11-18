import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
export interface Notificacao {
  id: string;
  destinatarioId: string;
  tipo: 'LANCAMENTO_PROXIMO' | 'LANCAMENTO_ADIADO' | 'ADVERTENCIA';
  titulo: string;
  mensagem: string;
  contextoOrigem: string;
  geradaEm: string;
  lidaEm: string | null;
}
@Injectable({ providedIn: 'root' })
export class NotificacoesService {
  private readonly http = inject(HttpClient);
  listar(): Observable<Notificacao[]> { return this.http.get<Notificacao[]>('/api/notificacoes'); }
  marcarLida(id: string): Observable<Notificacao> {
    return this.http.patch<Notificacao>(`/api/notificacoes/${id}/leitura`, {});
  }
}
