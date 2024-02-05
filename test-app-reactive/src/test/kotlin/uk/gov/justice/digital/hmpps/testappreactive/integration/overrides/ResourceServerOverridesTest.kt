package uk.gov.justice.digital.hmpps.testappreactive.integration.overrides

import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.testappreactive.integration.IntegrationTestBase

@TestPropertySource(properties = ["test.type=override"])
class ResourceServerOverridesTest : IntegrationTestBase() {

  @Test
  fun `should return unauthorized with overridden resource server`() {
    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }
}
