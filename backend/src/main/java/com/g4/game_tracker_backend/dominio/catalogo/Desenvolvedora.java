package com.g4.game_tracker_backend.dominio.catalogo;

import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

public final class Desenvolvedora {

    private final UUID id;
    private final String nome;
    private final String biografia;
    private final String site;
    private final String redesSociais;
    private final String pais;
    private final boolean ativa;

    private Desenvolvedora(UUID id, String nome, String biografia, String site, String redesSociais,
            String pais, boolean ativa) {
        this.id = Objects.requireNonNull(id, "O identificador da desenvolvedora é obrigatório");
        this.nome = texto(nome, "O nome da desenvolvedora é obrigatório");
        this.biografia = limitar(biografia, 2000, "A biografia da desenvolvedora deve ter no máximo 2000 caracteres");
        this.site = textoOpcional(site);
        this.redesSociais = textoOpcional(redesSociais);
        this.pais = limitar(pais, 120, "O país deve ter no máximo 120 caracteres");
        this.ativa = ativa;
    }

    public static Desenvolvedora criar(String nome, String biografia, String site, String redesSociais,
            String pais) {
        return new Desenvolvedora(UUID.randomUUID(), nome, biografia, site, redesSociais, pais, true);
    }

    public static Desenvolvedora reconstituir(UUID id, String nome, String biografia, String site,
            String redesSociais, String pais, boolean ativa) {
        return new Desenvolvedora(id, nome, biografia, site, redesSociais, pais, ativa);
    }

    public Desenvolvedora atualizarPerfil(String biografia, String site, String redesSociais, String pais) {
        return new Desenvolvedora(id, nome, biografia, site, redesSociais, pais, ativa);
    }

    public Desenvolvedora desativar() {
        return new Desenvolvedora(id, nome, biografia, site, redesSociais, pais, false);
    }

    private static String texto(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException(mensagem);
        }
        return valor.trim();
    }

    private static String textoOpcional(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static String limitar(String valor, int limite, String mensagem) {
        String normalizado = textoOpcional(valor);
        if (normalizado.length() > limite) {
            throw new RegraDeNegocioException(mensagem);
        }
        return normalizado;
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String biografia() {
        return biografia;
    }

    public String site() {
        return site;
    }

    public String redesSociais() {
        return redesSociais;
    }

    public String pais() {
        return pais;
    }

    public boolean ativa() {
        return ativa;
    }
}
