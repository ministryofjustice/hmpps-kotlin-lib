package uk.gov.justice.digital.hmpps.testappreactive.integration.resource

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.testappreactive.integration.IntegrationTestBase

class TestAppUnprotectedResourceIntegrationTest : IntegrationTestBase() {

  @Nested
  inner class AuthEndpoint {

    @Test
    fun `should allow anyone in`() {
      webTestClient.get()
        .uri("/unprotected/auth/token")
        .exchange()
        .expectStatus()
        .isOk
    }

    @Test
    fun `should return default message if no token`() {
      webTestClient.get()
        .uri("/unprotected/auth/token")
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("greeting").isEqualTo("No authentication provided")
    }

    @Test
    fun `should return principal when user passed through`() {
      webTestClient.get()
        .uri("/unprotected/auth/token")
        .headers(setAuthorisation(roles = listOf("ROLE_TEST_APP")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("greeting").isEqualTo("Hello there AUTH_ADM")
    }

    @Test
    fun `should return principal when no user passed through`() {
      webTestClient.get()
        .uri("/unprotected/auth/token")
        .headers(setAuthorisation(user = null, roles = listOf("ROLE_TEST_APP")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("greeting").isEqualTo("Hello there test-client-id")
    }
  }
}
