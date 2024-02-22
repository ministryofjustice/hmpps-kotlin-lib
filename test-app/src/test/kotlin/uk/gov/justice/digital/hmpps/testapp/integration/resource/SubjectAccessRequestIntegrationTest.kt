package uk.gov.justice.digital.hmpps.testapp.integration.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.testapp.integration.IntegrationTestBase

class SubjectAccessRequestIntegrationTest : IntegrationTestBase() {
  @Nested
  @DisplayName("/subject-access-request")
  inner class SubjectAccessRequestEndpoint {

    @Nested
    inner class Security {
      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .headers(setAuthorisation(roles = listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {
      @Test
      fun `should return data if prisoner exists`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.content.prisonerNumber").isEqualTo("A12345")
          .jsonPath("$.content.commentText").isEqualTo("some useful comment")
      }

      @Test
      fun `should return 204 if no prisoner data exists`() {
        webTestClient.get().uri("/subject-access-request?prn=B12345C")
          .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
          .exchange()
          .expectStatus().isNoContent
      }

      @Test
      fun `should return success if both prn and crn supplied`() {
        webTestClient.get().uri("/subject-access-request?prn=B12345C&crn=1234")
          .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
          .exchange()
          .expectStatus().isNoContent
      }

      @Test
      fun `should return 209 if no prn supplied`() {
        webTestClient.get().uri("/subject-access-request?crn=AB12345C")
          .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
          .exchange()
          .expectStatus().isEqualTo(209)
      }
    }
  }
}
