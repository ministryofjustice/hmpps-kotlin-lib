package uk.gov.justice.hmpps.kotlin.customize

@DslMarker
annotation class ResourceServerConfigurationCustomizerDslMarker

@ResourceServerConfigurationCustomizerDslMarker
interface ResourceServerConfigurationCustomizerDsl {
  @UnauthorizedRequestPathCustomizerDslMarker
  fun unauthorizedRequestPaths(
    addPaths: Set<String> = setOf(),
    includeDefaults: Boolean = true,
  ): UnauthorizedRequestPathsCustomizer
}

@DslMarker
annotation class UnauthorizedRequestPathCustomizerDslMarker

@UnauthorizedRequestPathCustomizerDslMarker
interface UnauthorizedRequestPathCustomizerDsl {
  val addPaths: Set<String>
  val includeDefaults: Boolean
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

  override fun unauthorizedRequestPaths(
    addPaths: Set<String>,
    includeDefaults: Boolean,
  ) =
    UnauthorizedRequestPathsCustomizerBuilder(
      addPaths,
      includeDefaults,
    )
      .build()
      .also { unauthorizedRequestPathsCustomizer = it }

  fun build(): ResourceServerConfigurationCustomizer =
    ResourceServerConfigurationCustomizer()
      .also { it.unauthorizedRequestPathsCustomizer = unauthorizedRequestPathsCustomizer }
}

class UnauthorizedRequestPathsCustomizer(
  val unauthorizedRequestPaths: Set<String> = setOf(),
)

class UnauthorizedRequestPathsCustomizerBuilder(
  override val addPaths: Set<String> = setOf(),
  override val includeDefaults: Boolean = true,
) : UnauthorizedRequestPathCustomizerDsl {
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
