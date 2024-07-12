package uk.gov.justice.digital.hmpps.testapp.integration.service

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.testapp.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.testapp.integration.wiremock.PrisonApiExtension.Companion.prisonApi
import uk.gov.justice.hmpps.test.kotlin.auth.HmppsAuthApiExtension.Companion.hmppsAuth

class PrisonApiServiceTest : IntegrationTestBase() {
  @Test
  fun `should supply authentication token`() {
    hmppsAuth.stubGrantToken()
    prisonApi.stubGetPrisonerLatestBooking("A1234AA")

    webTestClient.get()
      .uri("/prisoner/A1234AA/booking")
      .headers(setAuthorisation(roles = listOf("ROLE_TEST_APP")))
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("bookingId").isEqualTo(12345)

    prisonApi.verify(
      getRequestedFor(urlEqualTo("/api/offender/A1234AA"))
        .withHeader("Authorization", WireMock.equalTo("Bearer ABCDE")),
    )
  }
}
