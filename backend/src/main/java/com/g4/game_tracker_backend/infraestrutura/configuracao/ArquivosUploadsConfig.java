package com.g4.game_tracker_backend.infraestrutura.configuracao;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ArquivosUploadsConfig implements WebMvcConfigurer {

    private final String diretorio;

    public ArquivosUploadsConfig(@Value("${app.uploads.catalogo-diretorio:uploads/catalogo}") String diretorio) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize().toUri().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/catalogo/**")
                .addResourceLocations(diretorio);

        String pastaDoForum = Path.of("uploads", "forum").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/forum/**")
                .addResourceLocations(pastaDoForum.endsWith("/") ? pastaDoForum : pastaDoForum + "/");

        String pastaDoPerfil = Path.of("uploads", "perfil").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/perfil/**")
                .addResourceLocations(pastaDoPerfil.endsWith("/") ? pastaDoPerfil : pastaDoPerfil + "/");
    }
}