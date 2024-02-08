package uk.gov.justice.hmpps.kotlin.auth.dsl

@DslMarker
annotation class UnauthorizedRequestPathCustomizerDslMarker

/**
 * Part of the [ResourceServerConfigurationCustomizerDsl] DSL.
 *
 * To create a new instance of [UnauthorizedRequestPathsCustomizer], use the [ResourceServerConfigurationCustomizer.Companion.build] method, e.g.
 *
 * <pre>
 *   @Bean
 *   fun configurationCustomizer() = ResourceServerConfigurationCustomizer.build {
 *     unauthorizedRequestPaths {
 *       addPaths(setOf("/my-unauthorized-path"))
 *       ...
 *     }
 *   }
 * </pre>
 *
 * The default values for the available customizations can be found in the [UnauthorizedRequestPathsCustomizerBuilder] class.
 *
 * Note that by default the paths found in [UnauthorizedRequestPathsCustomizerBuilder.defaultUnauthorizedRequestPaths] are unauthorized.
 */
@UnauthorizedRequestPathCustomizerDslMarker
interface UnauthorizedRequestPathCustomizerDsl {
  /**
   * Paths to be added to the default list of unauthorized paths.
   */
  var addPaths: Set<String>

  /**
   * Whether to include the default unauthorized paths, defaults to <pre>true</pre>.
   */
  var includeDefaults: Boolean
}

class UnauthorizedRequestPathsCustomizer(
  val unauthorizedRequestPaths: Set<String> = setOf(),
)

class UnauthorizedRequestPathsCustomizerBuilder : UnauthorizedRequestPathCustomizerDsl {
  override var addPaths: Set<String> = setOf()
  override var includeDefaults: Boolean = true

  fun build(): UnauthorizedRequestPathsCustomizer {
    val paths = mutableSetOf<String>()
      .apply {
        addAll(addPaths)
        if (includeDefaults) addAll(defaultUnauthorizedRequestPaths())
      }
    return UnauthorizedRequestPathsCustomizer(paths)
  }

  private fun defaultUnauthorizedRequestPaths() = listOf(
    "/webjars/**", "/favicon.ico", "/csrf",
    "/health/**", "/info", "/h2-console/**",
    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
  )
}
