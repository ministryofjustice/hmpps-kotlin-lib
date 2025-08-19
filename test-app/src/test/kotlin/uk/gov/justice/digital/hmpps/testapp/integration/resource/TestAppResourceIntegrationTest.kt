package uk.gov.justice.digital.hmpps.testapp.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.testapp.health.HmppsAuthHealthPing
import uk.gov.justice.digital.hmpps.testapp.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.testapp.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.testapp.integration.wiremock.PrisonApiExtension
import java.time.LocalDate

class TestAppResourceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var hmppsAuth: HmppsAuthHealthPing

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
        .headers(setAuthorisation(roles = listOf("ROLE_TEST_APP")))
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
        .headers(setAuthorisation(roles = listOf("TEST_APP")))
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
        .headers(setAuthorisation(user = null, roles = listOf("ROLE_TEST_APP")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("greeting").isEqualTo("Hello there test-client-id")
    }
  }

  @Nested
  inner class BookingEndpointWithUSerContext {

    @BeforeEach
    fun setup() {
      HmppsAuthApiExtension.hmppsAuth.resetAll()
      // The HMPPS Auth Token Endpoint stub will only match a request containing the provided
      // username in the request body.
      HmppsAuthApiExtension.hmppsAuth.stubUsernameEnhancedGrantToken("AUTH_ADM")
      PrisonApiExtension.prisonApi.stubGetPrisonerLatestBooking("ABC123C")
    }

    @Test
    fun `should pass username in context when requesting client credentials token`() {
      webTestClient.get().uri { uriBuilder ->
        uriBuilder
          .path("/prisoner/{prisonNumber}/booking")
          .queryParam("userContext", true)
          .build("ABC123C")
      }
        .headers(setAuthorisation(roles = listOf("ROLE_TEST_APP")))
        .exchange()
        .expectStatus()
        .isOk
    }
  }
}
