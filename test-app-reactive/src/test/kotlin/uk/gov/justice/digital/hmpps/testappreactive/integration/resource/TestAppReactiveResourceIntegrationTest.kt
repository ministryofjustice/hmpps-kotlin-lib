package uk.gov.justice.digital.hmpps.testappreactive.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.testappreactive.integration.IntegrationTestBase
import java.time.LocalDate

class TestAppReactiveResourceIntegrationTest : IntegrationTestBase() {

  @Nested
  inner class TimeEndpoint {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/time")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/time")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("/time")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK`() {
      webTestClient.get()
        .uri("/time")
        .headers(setAuthorisation(roles = listOf("ROLE_TEST_APP_REACTIVE")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("$").value<String> {
          assertThat(it).startsWith("${LocalDate.now()}")
        }
    }
  }

  @Nested
  inner class AuthEndpoint {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/auth/token")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/auth/token")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("/auth/token")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return principal when user passed through`() {
      webTestClient.get()
        .uri("/auth/token")
        .headers(setAuthorisation(roles = listOf("ROLE_TEST_APP_REACTIVE")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("greeting").isEqualTo("Hello there AUTH_ADM")
    }

    @Test
    fun `should add ROLE_ to roles if not provided`() {
      webTestClient.get()
        .uri("/auth/token")
        .headers(setAuthorisation(roles = listOf("TEST_APP_REACTIVE")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("greeting").isEqualTo("Hello there AUTH_ADM")
    }

    @Test
    fun `should return principal when no user passed through`() {
      webTestClient.get()
        .uri("/auth/token")
        .headers(setAuthorisation(user = null, roles = listOf("ROLE_TEST_APP_REACTIVE")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("greeting").isEqualTo("Hello there test-client-id")
    }
  }
}
