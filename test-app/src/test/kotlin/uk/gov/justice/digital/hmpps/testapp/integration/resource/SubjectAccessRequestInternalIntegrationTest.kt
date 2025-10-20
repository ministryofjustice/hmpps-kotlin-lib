package uk.gov.justice.digital.hmpps.testapp.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.testapp.integration.IntegrationTestBase

/**
 * This class tests the functionality of the HmppsSubjectAccessRequestController
 *
 * It should not be copied into other projects to test that their service implementations are
 * correct - please see SubjectAccessRequestSampleIntegrationTest instead. Service implementations should test that
 * the endpoint exists and their content is returned.
 *
 * Other scenarios such as 209 or 400 scenarions should be left to this class to test as they are subject to change.
 */
class SubjectAccessRequestInternalIntegrationTest : IntegrationTestBase() {
  @Nested
  @DisplayName("/subject-access-request")
  inner class SubjectAccessRequestEndpoint {

    @Nested
    inner class Security {
      @Test
      fun `get data access forbidden when no authority`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `get template access forbidden when no authority`() {
        webTestClient.get().uri("/subject-access-request/template")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `get data access forbidden when no role`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .headers(setAuthorisation(roles = listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `get template access forbidden when no role`() {
        webTestClient.get().uri("/subject-access-request/template")
          .headers(setAuthorisation(roles = listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `get data access forbidden with wrong role`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `get template access forbidden with wrong role`() {
        webTestClient.get().uri("/subject-access-request/template")
          .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {
      @Test
      fun `should return data if prisoner exists`() {
        // service will return data for prisoners that start with A
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.content.prisonerNumber").isEqualTo("A12345")
          .jsonPath("$.content.commentText").isEqualTo("some useful comment")
      }

      @Test
      fun `should return success for additional access role`() {
        // service will return not found for prisoners that don't start with A
        webTestClient.get().uri("/subject-access-request?prn=B12345C")
          .headers(setAuthorisation(roles = listOf("ROLE_TEST_DATA_ACCESS")))
          .exchange()
          .expectStatus().isNoContent
      }

      @Test
      fun `should return 204 if no prisoner data exists`() {
        // service will return not found for prisoners that don't start with A
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

      @Test
      fun `should return 400 if no prn or crn supplied`() {
        webTestClient.get().uri("/subject-access-request")
          .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
          .exchange()
          .expectStatus().isEqualTo(400)
      }
    }

    @Test
    fun `should return expected template`() {
      webTestClient.get().uri("/subject-access-request/template")
        .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
        .exchange()
        .expectStatus().isEqualTo(200)
        .expectHeader().contentType(MediaType.TEXT_PLAIN_VALUE)
        .expectBody(String::class.java)
        .value { body ->
          assertThat(body).isEqualTo("""<h1>Subject Access Request Test Template</h1>""")
        }
    }
  }
}
