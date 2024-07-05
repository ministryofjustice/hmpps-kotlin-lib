package uk.gov.justice.digital.hmpps.testapp.integration.auth.overrides

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import uk.gov.justice.digital.hmpps.testapp.integration.IntegrationTestBase
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareTokenConverter

@Import(ResourceServerOverridesTest.OverrideConfiguration::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["spring.main.allow-bean-definition-overriding=true"],
)
class ResourceServerOverridesTest : IntegrationTestBase() {

  @TestConfiguration
  @EnableWebSecurity
  @EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
  class OverrideConfiguration {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
      http {
        sessionManagement { SessionCreationPolicy.STATELESS }
        headers { frameOptions { sameOrigin = true } }
        csrf { disable() }
        authorizeHttpRequests {
          // This resource server doesn't have any unauthorized request paths unlike the library's default resource server
          authorize(anyRequest, authenticated)
        }
        oauth2ResourceServer { jwt { jwtAuthenticationConverter = AuthAwareTokenConverter() } }
      }.let { http.build() }
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
