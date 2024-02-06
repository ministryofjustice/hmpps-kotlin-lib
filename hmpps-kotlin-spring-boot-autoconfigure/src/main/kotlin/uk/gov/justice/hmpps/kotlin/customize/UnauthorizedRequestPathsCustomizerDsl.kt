package uk.gov.justice.hmpps.kotlin.customize

@DslMarker
annotation class UnauthorizedRequestPathCustomizerDslMarker

@UnauthorizedRequestPathCustomizerDslMarker
interface UnauthorizedRequestPathCustomizerDsl {
  fun addPaths(paths: Set<String>): UnauthorizedRequestPathsCustomizerBuilder
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
