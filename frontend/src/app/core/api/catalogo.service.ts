import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { forkJoin, map, Observable } from 'rxjs';

export interface Desenvolvedora {
  id: string;
  nome: string;
  biografia: string;
  site: string;
  redesSociais: string;
  pais: string;
  ativa: boolean;
}

export interface Genero {
  id: string;
  nome: string;
}

export interface CadastroJogo {
  titulo: string;
  tituloOriginal: string;
  resumo: string;
  /** Ignorado no cadastro manual: o backend usa a desenvolvedora vinculada ao desenvolvedor. */
  desenvolvedoraId: string | null;
  generosIds: string[];
  dataLancamento: string | null;
  capaUrl: string;
  trailerUrl: string;
  metadados: Record<string, string | number | boolean | string[]>;
}

export interface Jogo {
  id: string;
  titulo: string;
  tituloOriginal: string;
  slug: string;
  resumo: string;
  desenvolvedoraId: string;
  generosIds: string[];
  dataLancamento: string | null;
  capaUrl: string;
  trailerUrl: string;
  metadadosJson: string;
  origem: string;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);

  listarDesenvolvedoras(): Observable<Desenvolvedora[]> {
    return this.http.get<Desenvolvedora[]>('/api/catalogo/desenvolvedoras');
  }

  listarGeneros(): Observable<Genero[]> {
    return this.http.get<Genero[]>('/api/catalogo/generos');
  }

  cadastrarJogo(dados: CadastroJogo): Observable<Jogo> {
    return this.http.post<Jogo>('/api/catalogo/jogos', dados);
  }

  buscarJogo(id: string): Observable<Jogo> {
    return this.http.get<Jogo>(`/api/catalogo/jogos/${id}`);
  }

  atualizarJogo(id: string, dados: CadastroJogo): Observable<Jogo> {
    return this.http.put<Jogo>(`/api/catalogo/jogos/${id}`, dados);
  }

  excluirJogo(id: string): Observable<Jogo> {
    return this.http.delete<Jogo>(`/api/catalogo/jogos/${id}`);
  }

  importarSteam(termo: string): Observable<Jogo> {
    return this.http.post<Jogo>('/api/catalogo/jogos/importacao/steam', { termo });
  }

  atualizarDesenvolvedora(id: string, dados: Omit<Desenvolvedora, 'id' | 'nome' | 'ativa'>): Observable<Desenvolvedora> {
    return this.http.put<Desenvolvedora>(`/api/catalogo/desenvolvedoras/${id}`, dados);
  }

  excluirDesenvolvedora(id: string): Observable<Desenvolvedora> {
    return this.http.delete<Desenvolvedora>(`/api/catalogo/desenvolvedoras/${id}`);
  }

  enviarCapa(imagem: File): Observable<{ caminho: string }> {
    const dados = new FormData();
    dados.append('imagem', imagem);
    return this.http.post<{ caminho: string }>('/api/catalogo/imagens', dados);
  }

  enviarGaleria(imagens: File[]): Observable<string[]> {
    return forkJoin(imagens.map((imagem) => this.enviarCapa(imagem))).pipe(
      map((respostas) => respostas.map((resposta) => resposta.caminho)),
    );
  }

  listarJogos(titulo?: string): Observable<Jogo[]> {
    let params = new HttpParams();
    if (titulo) params = params.set('titulo', titulo);
    return this.http.get<Jogo[]>('/api/catalogo/jogos', { params });
  }
}
