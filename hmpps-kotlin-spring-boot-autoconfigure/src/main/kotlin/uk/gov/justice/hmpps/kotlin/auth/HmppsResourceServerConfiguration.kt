package uk.gov.justice.hmpps.kotlin.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@ConditionalOnWebApplication(type = SERVLET)
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@EnableCaching
class HmppsResourceServerConfiguration {
  @ConditionalOnMissingFilterBean
  @Bean
  fun hmppsSecurityFilterChain(http: HttpSecurity): SecurityFilterChain =
    http {
      sessionManagement { SessionCreationPolicy.STATELESS }
      headers { frameOptions { sameOrigin = true } }
      csrf { disable() }
      authorizeHttpRequests {
        unauthorizedRequestPaths().forEach { authorize(it, permitAll) }
        authorize(anyRequest, authenticated)
      }
      oauth2ResourceServer {
        jwt { jwtAuthenticationConverter = AuthAwareTokenConverter() }
      }
    }
      .let { http.build() }

  @ConditionalOnMissingBean
  @Bean
  fun locallyCachedJwtDecoder(
    @Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") jwkSetUri: String,
    cacheManager: CacheManager,
  ): JwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).cache(cacheManager.getCache("jwks")).build()
}

@Configuration
@ConditionalOnWebApplication(type = REACTIVE)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(useAuthorizationManager = false)
class HmppsReactiveResourceServerConfiguration {
  @ConditionalOnMissingFilterBean
  @Bean
  fun hmppsSecurityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
    http {
      csrf { disable() }
      authorizeExchange {
        unauthorizedRequestPaths().forEach { authorize(it, permitAll) }
        authorize(anyExchange, authenticated)
      }
      oauth2ResourceServer { jwt { jwtAuthenticationConverter = AuthAwareReactiveTokenConverter() } }
    }
}

private fun unauthorizedRequestPaths() = listOf(
  "/webjars/**", "/favicon.ico", "/csrf",
  "/health/**", "/info", "/h2-console/**",
  "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
)