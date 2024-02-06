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

  /**
   * A customizer for setting a default role for all authorized endpoints. See [AnyRequestRoleCustomizerDsl] for more details.
   */
  @AnyRequestRoleCustomizerDslMarker
  fun anyRequestRole(dsl: AnyRequestRoleCustomizerDsl.() -> Unit): AnyRequestRoleCustomizer
}

class ResourceServerConfigurationCustomizer {
  lateinit var unauthorizedRequestPathsCustomizer: UnauthorizedRequestPathsCustomizer
  lateinit var anyRequestRoleCustomizer: AnyRequestRoleCustomizer

  companion object {
    fun build(dsl: ResourceServerConfigurationCustomizerDsl.() -> Unit): ResourceServerConfigurationCustomizer =
      ResourceServerConfigurationCustomizerBuilder()
        .apply(dsl)
        .build()
  }
}

class ResourceServerConfigurationCustomizerBuilder : ResourceServerConfigurationCustomizerDsl {
  private var unauthorizedRequestPathsCustomizer = UnauthorizedRequestPathsCustomizerBuilder().build()
  private var anyRequestRoleCustomizer = AnyRequestRoleCustomizerBuilder().build()

  override fun unauthorizedRequestPaths(dsl: UnauthorizedRequestPathCustomizerDsl.() -> Unit) =
    UnauthorizedRequestPathsCustomizerBuilder()
      .apply(dsl)
      .build()
      .also { unauthorizedRequestPathsCustomizer = it }

  override fun anyRequestRole(dsl: AnyRequestRoleCustomizerDsl.() -> Unit): AnyRequestRoleCustomizer =
    AnyRequestRoleCustomizerBuilder()
      .apply(dsl)
      .build()
      .also { anyRequestRoleCustomizer = it }

  fun build(): ResourceServerConfigurationCustomizer =
    ResourceServerConfigurationCustomizer()
      .also {
        it.unauthorizedRequestPathsCustomizer = unauthorizedRequestPathsCustomizer
        it.anyRequestRoleCustomizer = anyRequestRoleCustomizer
      }
}
