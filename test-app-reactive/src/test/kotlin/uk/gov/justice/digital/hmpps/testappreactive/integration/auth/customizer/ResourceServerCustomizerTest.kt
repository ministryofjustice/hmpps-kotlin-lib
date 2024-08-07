package uk.gov.justice.digital.hmpps.testappreactive.integration.auth.customizer

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.testappreactive.integration.IntegrationTestBase
import uk.gov.justice.hmpps.kotlin.auth.dsl.ResourceServerConfigurationCustomizer

@Import(ResourceServerCustomizerTest.CustomizerConfiguration::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  // allow overriding of the ResourceServerConfigurationCustomizer bean definition
  properties = ["spring.main.allow-bean-definition-overriding=true"],
)
class ResourceServerCustomizerTest : IntegrationTestBase() {

  @TestConfiguration
  class CustomizerConfiguration {
    @Bean
    fun resourceServerCustomizer() = ResourceServerConfigurationCustomizer {
      unauthorizedRequestPaths {
        addPaths = setOf("/info")
        includeDefaults = false
      }
      anyRequestRole { defaultRole = "ANY_REQUEST" }
    }
  }

  @Test
  fun `should return unauthorized when defaults are removed`() {
    stubPingWithResponse(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return OK when added to customizer`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `should apply the default role to all authorised endpoints`() {
    stubPingWithResponse(200)

    webTestClient.get()
      .uri("/health")
      .headers(setAuthorisation(roles = listOf("ROLE_ANY_REQUEST")))
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
