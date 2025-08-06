package uk.gov.justice.digital.hmpps.testapp.integration.auth.customizer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.ClientAuthorizationException
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider
import uk.gov.justice.digital.hmpps.testapp.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.testapp.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.testapp.service.PrisonApiService
import uk.gov.justice.hmpps.kotlin.auth.oAuth2AuthorizedClientProvider
import java.time.Duration

@Import(WebClientCustomizerTest.CustomizerConfiguration::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  // allow overriding of the ResourceServerConfigurationCustomizer bean definition
  properties = ["spring.main.allow-bean-definition-overriding=true"],
)
class WebClientCustomizerTest(
  @param:Autowired private val prisonApiService: PrisonApiService,
) : IntegrationTestBase() {

  @TestConfiguration
  class CustomizerConfiguration {
    @Bean
    fun authorizedClientProvider(): OAuth2AuthorizedClientProvider = oAuth2AuthorizedClientProvider(Duration.ofMillis(50))
  }

  @Test
  fun `should return OK when exceeding the client credentials client timeout`() {
    hmppsAuth.stubGrantToken(delayMs = 0)

    assertDoesNotThrow {
      prisonApiService.getOffenderBooking("ANY")
    }
  }

  @Test
  fun `should throw when exceeding the client credentials client timeout`() {
    hmppsAuth.stubGrantToken(delayMs = 100)

    assertThrows<ClientAuthorizationException> {
      prisonApiService.getOffenderBooking("ANY")
    }
  }
}
