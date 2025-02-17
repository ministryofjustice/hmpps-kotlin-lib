package uk.gov.justice.digital.hmpps.testapp.integration.auth.customizer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.testapp.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.testapp.resource.CustomAuthAwareAuthenticationTokenConverter
import uk.gov.justice.hmpps.kotlin.auth.dsl.ResourceServerConfigurationCustomizer
import java.time.LocalDate

@Import(CustomTokenConverterTest.CustomizerConfiguration::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  // allow overriding of the ResourceServerConfigurationCustomizer bean definition
  properties = ["spring.main.allow-bean-definition-overriding=true"],
)
class CustomTokenConverterTest : IntegrationTestBase() {

  private class CaseloadFinder {
    fun find() = "MDI"
  }

  @TestConfiguration
  class CustomizerConfiguration {
    @Bean
    fun resourceServerCustomizer() = ResourceServerConfigurationCustomizer {
      // Add a custom token converter that gets caseload from "somewhere", e.g. could be a bean calling another service
      oauth2 { tokenConverter = CustomAuthAwareAuthenticationTokenConverter { CaseloadFinder().find() } }
    }
  }

  @Test
  fun `should return OK for an unauthorized endpoint`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `should return unauthorized for a protected endpoint`() {
    webTestClient.get()
      .uri("/active-caseload")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden without a role`() {
    webTestClient.get()
      .uri("/active-caseload")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden with wrong role`() {
    webTestClient.get()
      .uri("/active-caseload")
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should exercise the custom token converter`() {
    webTestClient.get()
      .uri("/active-caseload")
      .headers(setAuthorisation(roles = listOf("ROLE_CUSTOM_CONVERTER")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("activeCaseload").isEqualTo("MDI")
  }

  @Test
  fun `should be OK calling endpoints relying on normal Auth token converter`() {
    webTestClient.get()
      .uri("/time")
      .headers(setAuthorisation(roles = listOf("ROLE_TEST_APP")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$").value<String> {
        assertThat(it).startsWith("${LocalDate.now()}")
      }
  }
}
