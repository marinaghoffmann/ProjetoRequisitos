package com.g4.game_tracker_backend.bdd;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Executa todos os cenários Gherkin de src/test/resources/features junto com "./mvnw test".
 *
 * Funcionalidades marcadas com @pendente ficam de fora até terem os steps implementados:
 * ao começar a sua funcionalidade, remova a tag do topo do .feature correspondente.
 * Relatório HTML gerado em target/cucumber-relatorio.html.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.g4.game_tracker_backend.bdd")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @pendente")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-relatorio.html")
public class ExecutarCenariosBddTest {
}
