package uk.gov.justice.hmpps.kotlin.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingFilterBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration
import org.springframework.cache.concurrent.ConcurrentMapCache
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
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import uk.gov.justice.hmpps.kotlin.auth.dsl.ResourceServerConfigurationCustomizer

@AutoConfigureBefore(WebMvcAutoConfiguration::class)
@Configuration
@ConditionalOnWebApplication(type = SERVLET)
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class HmppsResourceServerConfiguration {
  @ConditionalOnMissingFilterBean
  @Bean
  fun hmppsSecurityFilterChain(http: HttpSecurity, customizer: ResourceServerConfigurationCustomizer): SecurityFilterChain = http {
    sessionManagement { SessionCreationPolicy.STATELESS }
    headers { frameOptions { sameOrigin = true } }
    csrf { disable() }
    customizer.securityMatcherCustomizer.paths
      .takeIf { it.isNotEmpty() }
      ?.also { securityMatcher(*it.toTypedArray()) }
    authorizeHttpRequests {
      customizer.authorizeHttpRequestsCustomizer.dsl
        // override the entire authorizeHttpRequests DSL
        ?.also { dsl -> dsl.invoke(this) }
        // apply specific customizations to the default authorizeHttpRequests DSL
        ?: also {
          customizer.unauthorizedRequestPathsCustomizer.unauthorizedRequestPaths.forEach { authorize(it, permitAll) }
          customizer.anyRequestRoleCustomizer.defaultRole
            ?.also { authorize(anyRequest, hasRole(it)) }
            ?: also { authorize(anyRequest, authenticated) }
        }
    }
    oauth2ResourceServer {
      jwt { jwtAuthenticationConverter = customizer.oauth2Customizer.tokenConverter }
    }
  }
    .let { http.build() }

  @ConditionalOnMissingBean
  @Bean
  fun resourceServerConfigurationCustomizer(): ResourceServerConfigurationCustomizer = ResourceServerConfigurationCustomizer {}

  @ConditionalOnMissingBean
  @Bean
  fun locallyCachedJwtDecoder(
    @Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") jwkSetUri: String,
  ): JwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).cache(ConcurrentMapCache("jwks")).build()
}

@AutoConfigureBefore(WebFluxAutoConfiguration::class)
@Configuration
@ConditionalOnWebApplication(type = REACTIVE)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(useAuthorizationManager = false)
class HmppsReactiveResourceServerConfiguration {
  @ConditionalOnMissingFilterBean
  @Bean
  fun hmppsSecurityWebFilterChain(http: ServerHttpSecurity, customizer: ResourceServerConfigurationCustomizer): SecurityWebFilterChain = http {
    csrf { disable() }
    customizer.securityMatcherCustomizer.paths
      .takeIf { it.isNotEmpty() }
      ?.also { securityMatcher(ServerWebExchangeMatchers.pathMatchers(*it.toTypedArray())) }
    authorizeExchange {
      customizer.authorizeExchangeCustomizer.dsl
        // override the entire authorizeExchange DSL
        ?.also { dsl -> dsl.invoke(this) }
        // apply specific customizations to the default authorizeExchange DSL
        ?: also {
          customizer.unauthorizedRequestPathsCustomizer.unauthorizedRequestPaths.forEach { authorize(it, permitAll) }
          customizer.anyRequestRoleCustomizer.defaultRole
            ?.also { authorize(anyExchange, hasRole(it)) }
            ?: also { authorize(anyExchange, authenticated) }
        }
    }
    oauth2ResourceServer { jwt { jwtAuthenticationConverter = customizer.oauth2Customizer.reactiveTokenConverter } }
  }

  @ConditionalOnMissingBean
  @Bean
  fun resourceServerConfigurationCustomizer(): ResourceServerConfigurationCustomizer = ResourceServerConfigurationCustomizer {}
}
