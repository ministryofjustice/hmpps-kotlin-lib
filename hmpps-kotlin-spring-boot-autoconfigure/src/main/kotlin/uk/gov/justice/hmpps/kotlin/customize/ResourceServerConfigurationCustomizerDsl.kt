package uk.gov.justice.hmpps.kotlin.customize

@DslMarker
annotation class ResourceServerConfigurationCustomizerDslMarker

/**
 * A DSL to create a [ResourceServerConfigurationCustomizer].
 *
 * To create a new instance of [ResourceServerConfigurationCustomizer], use the [ResourceServerConfigurationCustomizer.Companion.build] method, e.g.
 *
 * <pre>
 *   @Bean
 *   fun configurationCustomizer() = ResourceServerConfigurationCustomizer.build {
 *     ...
 *   }
 * </pre>
 */
@ResourceServerConfigurationCustomizerDslMarker
interface ResourceServerConfigurationCustomizerDsl {
  /**
   * A customizer for unauthorized request paths. See [UnauthorizedRequestPathCustomizerDsl] for more details.
   */
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
