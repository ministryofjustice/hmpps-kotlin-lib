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
 * Note that by default the security matcher is empty so applies to all paths
 */
@SecurityMatcherCustomizerDslMarker
interface SecurityMatcherCustomizerDsl {
  /**
   * Overrides the default empty security marcher for a non-reactive servlet based application.
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
