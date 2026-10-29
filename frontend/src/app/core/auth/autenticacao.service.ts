import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

export type Papel = 'COMUM' | 'DESENVOLVEDOR' | 'MODERADOR' | 'ADMINISTRADOR';

export interface UsuarioAutenticado {
  id: string;
  nome: string;
  email: string;
  papel: Papel;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string;
  usuario: UsuarioAutenticado;
}

export interface CadastroUsuario {
  nome: string;
  email: string;
  senha: string;
}

@Injectable({ providedIn: 'root' })
export class AutenticacaoService {
  private readonly http = inject(HttpClient);
  private readonly chaveToken = 'game-tracker.access-token';
  private readonly chaveUsuario = 'game-tracker.usuario';

  readonly usuarioAtual = signal<UsuarioAutenticado | null>(this.lerUsuarioSalvo());

  tokenAtual(): string | null {
    return sessionStorage.getItem(this.chaveToken);
  }

  registrar(dados: CadastroUsuario): Observable<UsuarioAutenticado> {
    return this.http.post<UsuarioAutenticado>('/api/usuarios', dados);
  }

  entrar(email: string, senha: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { email, senha }).pipe(
      tap((resposta) => {
        sessionStorage.setItem(this.chaveToken, resposta.accessToken);
        sessionStorage.setItem(this.chaveUsuario, JSON.stringify(resposta.usuario));
        this.usuarioAtual.set(resposta.usuario);
      }),
    );
  }

  sair(): void {
    sessionStorage.removeItem(this.chaveToken);
    sessionStorage.removeItem(this.chaveUsuario);
    this.usuarioAtual.set(null);
  }

  private lerUsuarioSalvo(): UsuarioAutenticado | null {
    const salvo = sessionStorage.getItem(this.chaveUsuario);
    if (!salvo) return null;
    try {
      return JSON.parse(salvo) as UsuarioAutenticado;
    } catch {
      sessionStorage.removeItem(this.chaveUsuario);
      sessionStorage.removeItem(this.chaveToken);
      return null;
    }
  }
}
