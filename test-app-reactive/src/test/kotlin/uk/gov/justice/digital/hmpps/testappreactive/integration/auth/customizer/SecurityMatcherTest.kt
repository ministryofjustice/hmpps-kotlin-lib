package uk.gov.justice.digital.hmpps.testappreactive.integration.auth.customizer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import uk.gov.justice.digital.hmpps.testappreactive.integration.IntegrationTestBase
import uk.gov.justice.hmpps.kotlin.auth.HmppsReactiveResourceServerConfiguration
import uk.gov.justice.hmpps.kotlin.auth.dsl.ResourceServerConfigurationCustomizer
import java.time.LocalDate

@Import(SecurityMatcherTest.CustomizerConfiguration::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  // allow overriding of the ResourceServerConfigurationCustomizer bean definition
  properties = ["spring.main.allow-bean-definition-overriding=true"],
)
class SecurityMatcherTest : IntegrationTestBase() {
  @TestConfiguration
  class CustomizerConfiguration {

    // Define a customizer that only applies to certain endpoints using the custom security matcher
    @Bean
    fun customSecurityMatcherCustomizer() = ResourceServerConfigurationCustomizer {
      // Add a custom security matcher which requires the CUSTOM_SECURITY_MATCHER role
      securityMatcher { paths = listOf("/protected-by-custom-security-matcher") }
      anyRequestRole { defaultRole = "CUSTOM_SECURITY_MATCHER" }
    }

    // And this is the security filter that uses the security matcher customizer
    @Bean
    fun securityMatcherSecurityFilterChain(
      http: ServerHttpSecurity,
      @Qualifier("customSecurityMatcherCustomizer") customizer: ResourceServerConfigurationCustomizer,
    ): SecurityWebFilterChain = HmppsReactiveResourceServerConfiguration().hmppsSecurityWebFilterChain(http, customizer)

    // We have to explicitly define a standard customizer - this gets the library default and is a backstop for requests not
    // matching the security matcher, so would have the standard customizer for the app e.g. including unauthorized paths etc
    @Bean
    fun hmppsCustomizer() = ResourceServerConfigurationCustomizer {}

    // And we also have to explicitly define the standard filter chain or the library will "get out of the way" and
    // only use the custom resource server defined above
    @Bean
    fun hmppsSecurityFilterChain(
      http: ServerHttpSecurity,
      @Qualifier("hmppsCustomizer") customizer: ResourceServerConfigurationCustomizer,
    ): SecurityWebFilterChain = HmppsReactiveResourceServerConfiguration().hmppsSecurityWebFilterChain(http, customizer)
  }

  @Test
  fun `should return OK for an unauthorized endpoint`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `should return unauthorized for a protected endpoint`() {
    webTestClient.get()
      .uri("/protected-by-custom-security-matcher")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden without a role`() {
    webTestClient.get()
      .uri("/protected-by-custom-security-matcher")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden with wrong role`() {
    webTestClient.get()
      .uri("/protected-by-custom-security-matcher")
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should allow access to the protected endpoint`() {
    webTestClient.get()
      .uri("/protected-by-custom-security-matcher")
      .headers(setAuthorisation(roles = listOf("ROLE_CUSTOM_SECURITY_MATCHER")))
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `should be OK calling endpoints relying on the security filter chain with no security matcher`() {
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
