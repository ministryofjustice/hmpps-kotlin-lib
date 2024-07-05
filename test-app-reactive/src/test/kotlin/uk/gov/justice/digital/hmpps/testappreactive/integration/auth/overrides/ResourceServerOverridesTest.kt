package uk.gov.justice.digital.hmpps.testappreactive.integration.auth.overrides

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.web.server.SecurityWebFilterChain
import uk.gov.justice.digital.hmpps.testappreactive.integration.IntegrationTestBase
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareReactiveTokenConverter

@Import(ResourceServerOverridesTest.OverrideConfiguration::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["spring.main.allow-bean-definition-overriding=true"],
)
class ResourceServerOverridesTest : IntegrationTestBase() {

  @TestConfiguration
  @EnableWebFluxSecurity
  @EnableReactiveMethodSecurity(useAuthorizationManager = false)
  class OverrideConfiguration {
    @Bean
    fun filterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
      http {
        csrf { disable() }
        authorizeExchange {
          // This resource server doesn't have any unauthorized request paths unlike the library's default resource server
          authorize(anyExchange, authenticated)
        }
        oauth2ResourceServer { jwt { jwtAuthenticationConverter = AuthAwareReactiveTokenConverter() } }
      }
  }

  @Test
  fun `should return unauthorized with overridden resource server`() {
    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }
}
