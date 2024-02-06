package uk.gov.justice.hmpps.kotlin.customize

@DslMarker
annotation class ResourceServerConfigurationCustomizerDslMarker

@ResourceServerConfigurationCustomizerDslMarker
interface ResourceServerConfigurationCustomizerDsl {
  @UnauthorizedRequestPathCustomizerDslMarker
  fun unauthorizedRequestPaths(dsl: UnauthorizedRequestPathCustomizerDsl.() -> Unit): UnauthorizedRequestPathsCustomizer
}

@DslMarker
annotation class UnauthorizedRequestPathCustomizerDslMarker

@UnauthorizedRequestPathCustomizerDslMarker
interface UnauthorizedRequestPathCustomizerDsl {
  fun addPaths(paths: Set<String>): UnauthorizedRequestPathsCustomizerBuilder
  fun includeDefaults(include: Boolean): UnauthorizedRequestPathsCustomizerBuilder
}

class ResourceServerConfigurationCustomizer {
  lateinit var unauthorizedRequestPathsCustomizer: UnauthorizedRequestPathsCustomizer

  companion object {
    fun build(dsl: ResourceServerConfigurationCustomizerDsl.() -> Unit): ResourceServerConfigurationCustomizer =
      ResourceServerConfigurationCustomizerBuilder()
        .apply(dsl)
        .build()
  }
}

class ResourceServerConfigurationCustomizerBuilder : ResourceServerConfigurationCustomizerDsl {
  private var unauthorizedRequestPathsCustomizer = UnauthorizedRequestPathsCustomizerBuilder().build()

  override fun unauthorizedRequestPaths(dsl: UnauthorizedRequestPathCustomizerDsl.() -> Unit) =
    UnauthorizedRequestPathsCustomizerBuilder()
      .apply(dsl)
      .build()
      .also { unauthorizedRequestPathsCustomizer = it }

  fun build(): ResourceServerConfigurationCustomizer =
    ResourceServerConfigurationCustomizer()
      .also { it.unauthorizedRequestPathsCustomizer = unauthorizedRequestPathsCustomizer }
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
