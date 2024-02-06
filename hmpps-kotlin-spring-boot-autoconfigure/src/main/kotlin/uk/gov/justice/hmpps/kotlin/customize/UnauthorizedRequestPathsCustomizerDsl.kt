package uk.gov.justice.hmpps.kotlin.customize

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
  fun addPaths(paths: Set<String>): UnauthorizedRequestPathsCustomizerBuilder

  /**
   * Whether to include the default unauthorized paths, defaults to <pre>true</pre>.
   */
  fun includeDefaults(include: Boolean): UnauthorizedRequestPathsCustomizerBuilder
}

class UnauthorizedRequestPathsCustomizer(
  val unauthorizedRequestPaths: Set<String> = setOf(),
)

class UnauthorizedRequestPathsCustomizerBuilder : UnauthorizedRequestPathCustomizerDsl {
  private var addPaths: Set<String> = setOf()
  private var includeDefaults: Boolean = true

  override fun addPaths(paths: Set<String>): UnauthorizedRequestPathsCustomizerBuilder =
    this.apply {
      addPaths = paths
    }

  override fun includeDefaults(include: Boolean): UnauthorizedRequestPathsCustomizerBuilder =
    this.apply {
      includeDefaults = include
    }

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
