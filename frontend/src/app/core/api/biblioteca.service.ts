import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface ItemBiblioteca {
  jogoId: string;
  titulo: string;
  capaUrl: string | null;
  adicionadoEm: string;
}

@Injectable({ providedIn: 'root' })
export class BibliotecaService {
  private readonly http = inject(HttpClient);

  listar(): Observable<ItemBiblioteca[]> {
    return this.http.get<ItemBiblioteca[]>('/api/biblioteca');
  }

  adicionar(jogoId: string): Observable<ItemBiblioteca> {
    return this.http.post<ItemBiblioteca>(`/api/biblioteca/${jogoId}`, null);
  }

  remover(jogoId: string): Observable<void> {
    return this.http.delete<void>(`/api/biblioteca/${jogoId}`);
  }
}