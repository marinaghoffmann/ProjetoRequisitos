package com.g4.game_tracker_backend.aplicacao.moderacao;

/**
 * Template Method das ações moderativas: toda ação passa pela mesma sequência.
 * As etapas concretas dependem do tipo de conteúdo e da ação solicitada.
 */
public abstract class FluxoAcaoModerativa<T> {
    public final T executar() {
        validar();
        aplicar();
        T registro = registrar();
        notificar(registro);
        return registro;
    }

    protected abstract void validar();
    protected abstract void aplicar();
    protected abstract T registrar();
    protected abstract void notificar(T registro);
}
