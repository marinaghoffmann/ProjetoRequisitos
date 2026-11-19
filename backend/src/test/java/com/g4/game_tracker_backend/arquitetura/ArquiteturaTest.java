package com.g4.game_tracker_backend.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Regras da arquitetura limpa verificadas a cada build. Se um destes testes falhar,
 * alguma classe está importando de uma camada que não deveria.
 */
class ArquiteturaTest {

    private static final String BASE = "com.g4.game_tracker_backend";

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE);

    @Test
    void dominioNaoDependeDeOutrasCamadas() {
        noClasses().that().resideInAPackage("..dominio..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..aplicacao..", "..infraestrutura..", "..apresentacao..")
                .check(CLASSES);
    }

    @Test
    void dominioNaoDependeDeFrameworks() {
        noClasses().that().resideInAPackage("..dominio..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "org.hibernate..")
                .because("o domínio é Java puro; as entidades JPA ficam em infraestrutura.persistencia")
                .check(CLASSES);
    }

    @Test
    void aplicacaoNaoDependeDeInfraestruturaNemApresentacao() {
        noClasses().that().resideInAPackage("..aplicacao..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infraestrutura..", "..apresentacao..", "jakarta.persistence..")
                .check(CLASSES);
    }

    /**
     * Arquitetura limpa: casos de uso em Java puro. A transação e os eventos ficam atrás de portas
     * ({@code ExecutorDeTransacao}, {@code PublicadorDeEventos}) ou das anotações do projeto
     * ({@code @CasoDeUso}, {@code @Transacional}), e o Spring entra só pela infraestrutura
     * ({@code infraestrutura.configuracao}, {@code infraestrutura.transacao}, {@code infraestrutura.eventos}).
     */
    @Test
    void aplicacaoNaoDependeDoSpring() {
        noClasses().that().resideInAPackage("..aplicacao..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..")
                .because("os casos de uso não dependem de framework; o Spring entra só pela infraestrutura")
                .check(CLASSES);
    }

    /**
     * No Context Map o catálogo é upstream dos lançamentos (F-12): ele publica eventos (DataJogoAlterada,
     * JogoDesativado) e quem depende dele reage, nunca o contrário.
     */
    @Test
    void catalogoNaoDependeDosLancamentos() {
        noClasses().that().resideInAnyPackage("..dominio.catalogo..", "..aplicacao.catalogo..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..lancamentos..")
                .because("o catálogo é upstream dos lançamentos no Context Map")
                .check(CLASSES);
    }

    /**
     * F-01 RN 3: o catálogo decide entre exclusão lógica e definitiva sem conhecer quem guarda o Id do jogo.
     * Os vínculos chegam pela porta {@code VinculosDoJogo}, implementada na infraestrutura.
     */
    @Test
    void catalogoNaoDependeDosContextosQueOReferenciam() {
        noClasses().that().resideInAnyPackage("..dominio.catalogo..", "..aplicacao.catalogo..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..avaliacao..", "..biblioteca..", "..wishlist..", "..journal..", "..forum..",
                        "..marketplace..", "..descoberta..", "..moderacao..", "..lancamentos..")
                .because("o catálogo é upstream no Context Map; os vínculos chegam pela porta VinculosDoJogo")
                .check(CLASSES);
    }

    @Test
    void apresentacaoNaoAcessaInfraestrutura() {
        noClasses().that().resideInAPackage("..apresentacao..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infraestrutura..")
                .because("controllers chamam casos de uso da aplicação, nunca repositórios")
                .check(CLASSES);
    }
}
