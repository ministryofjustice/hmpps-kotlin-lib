package uk.gov.justice.hmpps.kotlin.auth.dsl

import org.springframework.security.config.web.server.AuthorizeExchangeDsl

@DslMarker
annotation class AuthorizeExchangeCustomizerDslMarker

/**
 * Part of the [ResourceServerConfigurationCustomizerDsl] DSL.
 *
 * To create a new instance of [AuthorizeExchangeCustomizer], use the [ResourceServerConfigurationCustomizer.Companion.invoke] method, e.g.
 *
 * ```
 *   @Bean
 *   fun resourceServerCustomizer() = ResourceServerConfigurationCustomizer {
 *     authorizeExchange {
 *       authorize(HttpMethod.GET, "/health", permitAll)
 *       authorize(HttpMethod.GET, "/events", hasRole("ROLE_EVENTS")
 *       authorize(HttpMethod.POST, "/events", hasRole("ROLE_EVENTS_RW")
 *       ...
 *     }
 *   }
 * ```
 *
 * Note that using this in conjunction with any customizations to authorizeExchange will cause the application startup to fail as that doesn't make sense.
 */
class AuthorizeExchangeCustomizer(
  var dsl: (AuthorizeExchangeDsl.() -> Unit)? = null,
)

class AuthorizeExchangeCustomizerBuilder(
  private var dsl: (AuthorizeExchangeDsl.() -> Unit)? = null,
) {

  fun build(): AuthorizeExchangeCustomizer = AuthorizeExchangeCustomizer(dsl)
}
