package uk.gov.justice.hmpps.kotlin.auth.dsl

@DslMarker
annotation class SecurityMatcherCustomizerDslMarker

/**
 * Part of the [ResourceServerConfigurationCustomizerDsl] DSL.
 *
 * To create a new instance of [SecurityMatcherCustomizer], use the [ResourceServerConfigurationCustomizer.Companion.invoke] method, e.g.
 *
 * ```
 *   @Bean
 *   fun resourceServerCustomizer() = ResourceServerConfigurationCustomizer {
 *     securityMatcher { listOf("/secured-paths") }
 *   }
 * ```
 *
 * By default, the security matcher is empty so is considered an `anyRequest` filter that applies to all paths.
 *
 * When implementing a security matcher in your customizer, this library won't automatically create the `SecurityFilterChain`
 * so you need to define one explicitly. Nor will it create an `anyRequest` filter chain so you also need to define one
 * of these or other paths will not be secured.
 *
 * Therefore, an implementation using a security matcher for certain paths might look something like this:
 *
 * ```
 *   // Applies the `specialRole` to any path matching the security matcher
 *   @Bean
 *   fun specialResourceServerCustomizer() = ResourceServerConfigurationCustomizer {
 *     securityMatcher { paths = listOf("/special/\*\*") }
 *     anyRequestRole { defaultRole = specialRole }
 *   }
 *
 *   // The filter chain using a security matcher needs an `@Order` higher than the HMPPS security filter chain so it's applied first
 *   @Order(1)
 *   @Bean
 *   fun specialSecurityFilterChain(
 *     http: HttpSecurity,
 *     specialResourceServerCustomizer: ResourceServerConfigurationCustomizer,
 *   ): SecurityFilterChain = HmppsResourceServerConfiguration().hmppsSecurityFilterChain(http, specialResourceServerCustomizer)
 *
 *   // The HMPPS filter chain does not have an `anyRequestRole` specified so is a catch-all `anyRequest` filter chain
 *   @Bean
 *   fun hmppsResourceServerCustomizer() = ResourceServerConfigurationCustomizer {
 *     unauthorizedRequestPaths {
 *       addPaths = setOf( "/queue-admin/retry-all-dlqs", )
 *     }
 *   }
 *
 *   // The HMPPS security filter chain gets the Spring default `@Order(LOWEST_PRECEDENCE)` so it's applied last
 *   @Bean
 *   fun hmppsSecurityFilterChain(
 *     http: HttpSecurity,
 *     hmppsResourceServerCustomizer: ResourceServerConfigurationCustomizer,
 *   ): SecurityFilterChain = HmppsResourceServerConfiguration().hmppsSecurityFilterChain(http, hmppsResourceServerCustomizer)
 * ```
 **/
@SecurityMatcherCustomizerDslMarker
interface SecurityMatcherCustomizerDsl {
  /**
   * The `SecurityFilterChain` will only be applied to these paths. Defaults to all paths.
   */
  var paths: List<String>
}

class SecurityMatcherCustomizer(
  val paths: List<String>,
)

class SecurityMatcherCustomizerBuilder : SecurityMatcherCustomizerDsl {
  override var paths: List<String> = emptyList()

  fun build(): SecurityMatcherCustomizer = SecurityMatcherCustomizer(paths)
}
