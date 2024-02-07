package uk.gov.justice.hmpps.kotlin.customize

import org.springframework.security.config.annotation.web.AuthorizeHttpRequestsDsl

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

  /**
   * Replaces the entire default authorizeHttpRequests block. See [AuthorizeHttpRequestsDsl] for more details.
   *
   * Note that trying to implement this override with other authorizeHttpRequests customizations will cause application startup to fail because that doesn't make sense.
   */
  @AuthorizeHttpRequestsCustomizerDslMarker
  fun authorizeHttpRequests(dsl: AuthorizeHttpRequestsDsl.() -> Unit): AuthorizeHttpRequestsCustomizer
}

class ResourceServerConfigurationCustomizer {
  lateinit var unauthorizedRequestPathsCustomizer: UnauthorizedRequestPathsCustomizer
  lateinit var anyRequestRoleCustomizer: AnyRequestRoleCustomizer
  lateinit var authorizeHttpRequestsCustomizer: AuthorizeHttpRequestsCustomizer

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
  private var authorizeHttpRequestsCustomizer = AuthorizeHttpRequestsCustomizerBuilder().build()
  private var overrideAuthorizeHttpRequests = false
  private var customizeAuthorizeHttpRequests = false

  override fun unauthorizedRequestPaths(dsl: UnauthorizedRequestPathCustomizerDsl.() -> Unit) =
    UnauthorizedRequestPathsCustomizerBuilder()
      .apply(dsl)
      .build()
      .also { unauthorizedRequestPathsCustomizer = it }
      .also { customizeAuthorizeHttpRequests = true }

  override fun anyRequestRole(dsl: AnyRequestRoleCustomizerDsl.() -> Unit): AnyRequestRoleCustomizer =
    AnyRequestRoleCustomizerBuilder()
      .apply(dsl)
      .build()
      .also { anyRequestRoleCustomizer = it }
      .also { customizeAuthorizeHttpRequests = true }

  override fun authorizeHttpRequests(dsl: AuthorizeHttpRequestsDsl.() -> Unit): AuthorizeHttpRequestsCustomizer =
    AuthorizeHttpRequestsCustomizerBuilder(dsl)
      .build()
      .also { authorizeHttpRequestsCustomizer = it }
      .also { overrideAuthorizeHttpRequests = true }

  fun build(): ResourceServerConfigurationCustomizer {
    if (overrideAuthorizeHttpRequests && customizeAuthorizeHttpRequests) {
      throw IllegalStateException("Cannot override the entire authorizeHttpRequests DSL and try to customize it at the same time.")
    }

    return ResourceServerConfigurationCustomizer()
      .also {
        it.unauthorizedRequestPathsCustomizer = unauthorizedRequestPathsCustomizer
        it.anyRequestRoleCustomizer = anyRequestRoleCustomizer
        it.authorizeHttpRequestsCustomizer = authorizeHttpRequestsCustomizer
      }
  }
}
