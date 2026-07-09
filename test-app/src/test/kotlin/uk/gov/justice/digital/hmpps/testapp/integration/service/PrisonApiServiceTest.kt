package uk.gov.justice.digital.hmpps.testapp.integration.service

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.testapp.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.testapp.integration.wiremock.PrisonApiExtension.Companion.prisonApi
import uk.gov.justice.digital.hmpps.testapp.service.PrisonApiService

class PrisonApiServiceTest(
  @Autowired private val prisonApiService: PrisonApiService,
) : IntegrationTestBase() {

  @Test
  fun `should supply authentication token`() {
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

  @Test
  fun `should supply authentication token even when calling service directly`() {
    prisonApi.stubGetPrisonerLatestBooking("A1234AA")

    assertThat(prisonApiService.getOffenderBooking("A1234AA"))
      .extracting("bookingId")
      .isEqualTo(12345L)

    prisonApi.verify(
      getRequestedFor(urlEqualTo("/api/offender/A1234AA"))
        .withHeader("Authorization", WireMock.equalTo("Bearer ABCDE")),
    )
  }
}
