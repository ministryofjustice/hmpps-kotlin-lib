package uk.gov.justice.digital.hmpps.testapp.integration.customizer

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.testapp.integration.IntegrationTestBase
import uk.gov.justice.hmpps.kotlin.customize.ResourceServerConfigurationCustomizer

@Import(ResourceServerCustomizerTest.CustomizerConfiguration::class)
class ResourceServerCustomizerTest : IntegrationTestBase() {

  @TestConfiguration
  class CustomizerConfiguration {
    @Bean
    fun configurationCustomizer() = ResourceServerConfigurationCustomizer.build {
      unauthorizedRequestPaths(
        addPaths = setOf("/info"),
        includeDefaults = false,
      )
    }
  }

  @Test
  fun `should return unauthorized when defaults are removed`() {
    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return OK when added to customizer`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
  }
}
