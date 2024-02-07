package uk.gov.justice.digital.hmpps.testapp.integration.customizer

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.testapp.integration.IntegrationTestBase
import uk.gov.justice.hmpps.kotlin.customize.ResourceServerConfigurationCustomizer

@Import(AuthenticateHttpRequestsCustomizerTest.CustomizerConfiguration::class)
class AuthenticateHttpRequestsCustomizerTest : IntegrationTestBase() {

  @TestConfiguration
  class CustomizerConfiguration {
    @Bean
    fun configurationCustomizer() = ResourceServerConfigurationCustomizer.build {
      authorizeHttpRequests {
        authorize("/info", hasRole("INFO"))
        authorize(anyRequest, hasRole("ANY_REQUEST"))
      }
    }
  }

  @Test
  fun `should return unauthorized as default unauthorized request paths are overridden`() {
    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return ok as this falls under the any request role`() {
    webTestClient.get()
      .uri("/health")
      .headers(setAuthorisation(roles = listOf("ROLE_ANY_REQUEST")))
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `should return unauthorized as we have protected on a role`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if we try with the wrong role`() {
    webTestClient.get()
      .uri("/info")
      .headers(setAuthorisation(roles = listOf("ROLE_ANY_REQUEST")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return OK if we use the correct role`() {
    webTestClient.get()
      .uri("/info")
      .headers(setAuthorisation(roles = listOf("ROLE_INFO")))
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `should not override a PreAuthorize with the default role`() {
    webTestClient.get()
      .uri("/time")
      .headers(setAuthorisation(roles = listOf("ROLE_ANY_REQUEST")))
      .exchange()
      .expectStatus()
      .isForbidden
  }
}
