import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface StatusDoSistema {
  aplicacao: string;
  status: string;
  verificadoEm: string;
}

@Injectable({ providedIn: 'root' })
export class StatusService {
  private readonly http = inject(HttpClient);

  consultar(): Observable<StatusDoSistema> {
    return this.http.get<StatusDoSistema>('/api/status');
  }
}
