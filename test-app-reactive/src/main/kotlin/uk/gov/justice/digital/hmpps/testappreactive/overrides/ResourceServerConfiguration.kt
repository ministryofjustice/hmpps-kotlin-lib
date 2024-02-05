package uk.gov.justice.digital.hmpps.testappreactive.overrides

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.web.server.SecurityWebFilterChain
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareReactiveTokenConverter

@Configuration
@ConditionalOnProperty(name = ["test.type"], havingValue = "override")
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(useAuthorizationManager = false)
class ResourceServerConfiguration {
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
