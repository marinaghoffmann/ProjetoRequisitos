package com.g4.game_tracker_backend.dominio.moderacao;

public enum AcaoModeracao {
    OCULTAR, EDITAR, MOVER, EXCLUIR, ENCERRAR_TOPICO, ADVERTIR_USUARIO,
    MARCAR_18, RETIRAR_18, REVERTER_OCULTACAO, REVERTER_EXCLUSAO, ARQUIVAR_DENUNCIA;

    /** Ocultar e excluir tiram o conteúdo do ar, então exigem justificativa. */
    public boolean exigeJustificativa() {
        return this == OCULTAR || this == EXCLUIR;
    }

    /** Ações que mudam a visibilidade e, por isso, os contadores e as mídias protegidas. */
    public boolean alteraVisibilidade() {
        return this == OCULTAR || this == EXCLUIR || this == REVERTER_OCULTACAO || this == REVERTER_EXCLUSAO;
    }

    /** Reverter ou retirar um aviso não julga as denúncias pendentes do alvo. */
    public boolean resolveDenuncias() {
        return this != REVERTER_OCULTACAO && this != REVERTER_EXCLUSAO && this != RETIRAR_18;
    }
}
