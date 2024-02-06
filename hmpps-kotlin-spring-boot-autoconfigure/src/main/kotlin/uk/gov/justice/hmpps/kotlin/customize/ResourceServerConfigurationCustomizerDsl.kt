package uk.gov.justice.hmpps.kotlin.customize

@DslMarker
annotation class ResourceServerConfigurationCustomizerDslMarker

@ResourceServerConfigurationCustomizerDslMarker
interface ResourceServerConfigurationCustomizerDsl {
  @UnauthorizedRequestPathCustomizerDslMarker
  fun unauthorizedRequestPaths(dsl: UnauthorizedRequestPathCustomizerDsl.() -> Unit): UnauthorizedRequestPathsCustomizer
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
