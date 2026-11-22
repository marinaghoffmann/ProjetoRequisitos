import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

export type Papel = 'COMUM' | 'DESENVOLVEDOR' | 'MODERADOR' | 'ADMINISTRADOR';

export interface UsuarioAutenticado {
  id: string;
  nome: string;
  email: string;
  papel: Papel;
  desenvolvedoraId: string | null;
  bio?: string | null;
  localizacao?: string | null;
  avatarUrl?: string | null;
  bannerUrl?: string | null;
}

export type TipoImagemPerfil = 'avatar' | 'banner';

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

export interface DadosPerfil {
  nome: string;
  bio: string;
  localizacao: string;
  bannerUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class AutenticacaoService {
  private readonly http = inject(HttpClient);
  private readonly chaveToken = 'game-tracker.access-token';
  private readonly chaveUsuario = 'game-tracker.usuario';

  readonly usuarioAtual = signal<UsuarioAutenticado | null>(this.lerUsuarioSalvo());

  /** Só controla o que a tela mostra; quem autoriza de fato é o backend. */
  temPapel(...papeis: Papel[]): boolean {
    const usuario = this.usuarioAtual();
    return !!usuario && papeis.includes(usuario.papel);
  }

  tokenAtual(): string | null {
    return localStorage.getItem(this.chaveToken);
  }

  registrar(dados: CadastroUsuario): Observable<UsuarioAutenticado> {
    return this.http.post<UsuarioAutenticado>('/api/usuarios', dados);
  }

  registrarPrivilegiado(dados: CadastroUsuario & { papel: Papel }): Observable<UsuarioAutenticado> {
    return this.http.post<UsuarioAutenticado>('/api/admin/usuarios', dados);
  }

  entrar(email: string, senha: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { email, senha }).pipe(
      tap((resposta) => {
        localStorage.setItem(this.chaveToken, resposta.accessToken);
        this.guardarUsuario(resposta.usuario);
      }),
    );
  }

  /** Busca os dados atuais do usuário logado (a sessão salva pode estar desatualizada). */
  recarregarUsuario(): Observable<UsuarioAutenticado> {
    return this.http.get<UsuarioAutenticado>('/api/auth/me').pipe(tap((usuario) => this.guardarUsuario(usuario)));
  }

  /** Atualiza nome, bio e localização; o usuário devolvido substitui o da sessão. */
  atualizarPerfil(dados: DadosPerfil): Observable<UsuarioAutenticado> {
    return this.http
      .put<UsuarioAutenticado>('/api/usuarios/me', dados)
      .pipe(tap((usuario) => this.guardarUsuario(usuario)));
  }

  /** Envia a foto de perfil ou o banner (multipart); o usuário devolvido substitui o da sessão. */
  enviarImagem(tipo: TipoImagemPerfil, arquivo: File): Observable<UsuarioAutenticado> {
    const corpo = new FormData();
    corpo.append('arquivo', arquivo);
    // Sem Content-Type manual: o navegador define o boundary do multipart.
    return this.http
      .post<UsuarioAutenticado>(`/api/usuarios/me/${tipo}`, corpo)
      .pipe(tap((usuario) => this.guardarUsuario(usuario)));
  }

  sair(): void {
    localStorage.removeItem(this.chaveToken);
    localStorage.removeItem(this.chaveUsuario);
    this.usuarioAtual.set(null);
  }

  private guardarUsuario(usuario: UsuarioAutenticado): void {
    localStorage.setItem(this.chaveUsuario, JSON.stringify(usuario));
    this.usuarioAtual.set(usuario);
  }

  private lerUsuarioSalvo(): UsuarioAutenticado | null {
    const salvo = localStorage.getItem(this.chaveUsuario);
    if (!salvo) return null;
    try {
      return JSON.parse(salvo) as UsuarioAutenticado;
    } catch {
      localStorage.removeItem(this.chaveUsuario);
      localStorage.removeItem(this.chaveToken);
      return null;
    }
  }
}
