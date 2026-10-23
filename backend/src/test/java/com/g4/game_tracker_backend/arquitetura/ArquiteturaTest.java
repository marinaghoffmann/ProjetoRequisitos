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

    @Test
    void apresentacaoNaoAcessaInfraestrutura() {
        noClasses().that().resideInAPackage("..apresentacao..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infraestrutura..")
                .because("controllers chamam casos de uso da aplicação, nunca repositórios")
                .check(CLASSES);
    }
}
