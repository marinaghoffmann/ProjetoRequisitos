import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export interface DadosConclusao {
  dataConclusao: string;
  plataforma: string | null;
  horasJogadas: number | null;
  concluido: boolean;
  platina: boolean;
}

export interface RegistroDeConclusao extends DadosConclusao {
  id: string;
}

export interface RegistroZeradoNaBiblioteca extends RegistroDeConclusao {
  jogoId: string;
  titulo: string;
  capaUrl: string | null;
}

@Injectable({ providedIn: 'root' })
export class ConclusoesService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/biblioteca';

  listarRegistros() {
    return this.http.get<RegistroZeradoNaBiblioteca[]>(`${this.base}/zerados/registros`);
  }

  historico(jogoId: string) {
    return this.http.get<RegistroDeConclusao[]>(this.doJogo(jogoId));
  }

  registrar(jogoId: string, dados: DadosConclusao) {
    return this.http.post<RegistroDeConclusao>(this.doJogo(jogoId), dados);
  }

  editar(id: string, dados: DadosConclusao, jogoId?: string) {
    return this.http.put<RegistroDeConclusao>(`${this.base}/conclusoes/${id}`, { ...dados, jogoId });
  }

  excluir(id: string) {
    return this.http.delete<void>(`${this.base}/conclusoes/${id}`);
  }

  private doJogo(jogoId: string): string {
    return `${this.base}/jogos/${jogoId}/conclusoes`;
  }
}
