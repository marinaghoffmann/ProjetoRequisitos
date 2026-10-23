package com.g4.game_tracker_backend.bdd;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.cucumber.spring.CucumberContextConfiguration;

/**
 * Sobe o contexto Spring uma única vez para todos os cenários, com o perfil "test"
 * (banco H2 em memória). Deve existir só UMA classe com @CucumberContextConfiguration:
 * os steps de cada funcionalidade apenas injetam os beans de que precisam.
 */
@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
public class ConfiguracaoSpringCucumber {
}
