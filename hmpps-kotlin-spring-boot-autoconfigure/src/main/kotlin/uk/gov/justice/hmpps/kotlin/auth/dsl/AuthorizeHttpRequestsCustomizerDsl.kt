package uk.gov.justice.hmpps.kotlin.auth.dsl

import org.springframework.security.config.annotation.web.AbstractRequestMatcherDsl
import org.springframework.security.config.annotation.web.AuthorizeHttpRequestsDsl

@DslMarker
annotation class AuthorizeHttpRequestsCustomizerDslMarker

/**
 * Part of the [ResourceServerConfigurationCustomizerDsl] DSL.
 *
 * To create a new instance of [AuthorizeHttpRequestsCustomizer], use the [ResourceServerConfigurationCustomizer.Companion.build] method, e.g.
 *
 * ```
 *   @Bean
 *   fun resourceServerCustomizer() = ResourceServerConfigurationCustomizer.build {
 *     authorizeHttpRequests {
 *       authorize(HttpMethod.GET, "/health", permitAll)
 *       authorize(HttpMethod.GET, "/events", hasRole("ROLE_EVENTS")
 *       authorize(HttpMethod.POST, "/events", hasRole("ROLE_EVENTS_RW")
 *       ...
 *     }
 *   }
 * ```
 *
 * Note that using this in conjunction with any customizations to authorizeHttpRequests will cause the application startup to fail as that doesn't make sense.
 */
class AuthorizeHttpRequestsCustomizer(
  var dsl: (AuthorizeHttpRequestsDsl.() -> Unit)? = null,
)

class AuthorizeHttpRequestsCustomizerBuilder(
  private var dsl: (AuthorizeHttpRequestsDsl.() -> Unit)? = null,
) : AbstractRequestMatcherDsl() {

  fun build(): AuthorizeHttpRequestsCustomizer {
    return AuthorizeHttpRequestsCustomizer(dsl)
  }
}
