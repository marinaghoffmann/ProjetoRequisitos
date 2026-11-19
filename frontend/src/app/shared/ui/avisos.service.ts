import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  texto: string;
  tipo: 'sucesso' | 'erro';
}

export interface PedidoDeConfirmacao {
  titulo: string;
  mensagem: string;
  confirmar?: string;
  perigo?: boolean;
}

const DURACAO_DO_TOAST_MS = 3500;

/** Toasts no canto inferior e o modal de confirmação das ações destrutivas, desenhados pelo shell. */
@Injectable({ providedIn: 'root' })
export class AvisosService {
  readonly toasts = signal<Toast[]>([]);
  readonly confirmacao = signal<(PedidoDeConfirmacao & { responder: (ok: boolean) => void }) | null>(null);
  private proximo = 1;

  sucesso(texto: string): void {
    this.mostrar(texto, 'sucesso');
  }

  erro(texto: string): void {
    this.mostrar(texto, 'erro');
  }

  fecharToast(id: number): void {
    this.toasts.update((lista) => lista.filter((t) => t.id !== id));
  }

  /** Abre o modal e resolve com true só se a pessoa confirmar. */
  confirmar(pedido: PedidoDeConfirmacao): Promise<boolean> {
    return new Promise((resolver) => {
      this.confirmacao.set({
        ...pedido,
        responder: (ok) => {
          this.confirmacao.set(null);
          resolver(ok);
        },
      });
    });
  }

  private mostrar(texto: string, tipo: Toast['tipo']): void {
    const id = this.proximo++;
    this.toasts.update((lista) => [...lista, { id, texto, tipo }]);
    setTimeout(() => this.fecharToast(id), DURACAO_DO_TOAST_MS);
  }
}
