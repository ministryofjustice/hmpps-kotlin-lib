package uk.gov.justice.hmpps.kotlin.customize

import org.springframework.security.config.annotation.web.AuthorizeHttpRequestsDsl
import org.springframework.security.config.web.server.AuthorizeExchangeDsl

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
   * Note that trying to implement this override with other authorization customizations will cause application startup to fail because that doesn't make sense.
   */
  @AuthorizeHttpRequestsCustomizerDslMarker
  fun authorizeHttpRequests(dsl: AuthorizeHttpRequestsDsl.() -> Unit): AuthorizeHttpRequestsCustomizer

  /**
   * Replaces the entire default authorizeExchange block for a reactive application. See [AuthorizeExchangeDsl] for more details.
   *
   * Note that trying to implement this override with other authorization customizations will cause application startup to fail because that doesn't make sense.
   */
  @AuthorizeExchangeCustomizerDslMarker
  fun authorizeExchange(dsl: AuthorizeExchangeDsl.() -> Unit): AuthorizeExchangeCustomizer
}

class ResourceServerConfigurationCustomizer {
  lateinit var unauthorizedRequestPathsCustomizer: UnauthorizedRequestPathsCustomizer
  lateinit var anyRequestRoleCustomizer: AnyRequestRoleCustomizer
  lateinit var authorizeHttpRequestsCustomizer: AuthorizeHttpRequestsCustomizer
  lateinit var authorizeExchangeCustomizer: AuthorizeExchangeCustomizer

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
  private var authorizeExchangeCustomizer = AuthorizeExchangeCustomizerBuilder().build()
  private var overrideAuthorizeHttpRequests = false
  private var overrideAuthorizeExchange = false
  private var customizeAuthorization = false

  override fun unauthorizedRequestPaths(dsl: UnauthorizedRequestPathCustomizerDsl.() -> Unit) =
    UnauthorizedRequestPathsCustomizerBuilder()
      .apply(dsl)
      .build()
      .also { unauthorizedRequestPathsCustomizer = it }
      .also { customizeAuthorization = true }

  override fun anyRequestRole(dsl: AnyRequestRoleCustomizerDsl.() -> Unit): AnyRequestRoleCustomizer =
    AnyRequestRoleCustomizerBuilder()
      .apply(dsl)
      .build()
      .also { anyRequestRoleCustomizer = it }
      .also { customizeAuthorization = true }

  override fun authorizeHttpRequests(dsl: AuthorizeHttpRequestsDsl.() -> Unit): AuthorizeHttpRequestsCustomizer =
    AuthorizeHttpRequestsCustomizerBuilder(dsl)
      .build()
      .also { authorizeHttpRequestsCustomizer = it }
      .also { overrideAuthorizeHttpRequests = true }

  override fun authorizeExchange(dsl: AuthorizeExchangeDsl.() -> Unit): AuthorizeExchangeCustomizer =
    AuthorizeExchangeCustomizerBuilder(dsl)
      .build()
      .also { authorizeExchangeCustomizer = it }
      .also { overrideAuthorizeExchange = true }

  fun build(): ResourceServerConfigurationCustomizer {
    validate()

    return ResourceServerConfigurationCustomizer()
      .also {
        it.unauthorizedRequestPathsCustomizer = unauthorizedRequestPathsCustomizer
        it.anyRequestRoleCustomizer = anyRequestRoleCustomizer
        it.authorizeHttpRequestsCustomizer = authorizeHttpRequestsCustomizer
        it.authorizeExchangeCustomizer = authorizeExchangeCustomizer
      }
  }

  private fun validate() {
    if (overrideAuthorizeHttpRequests && customizeAuthorization) {
      throw IllegalStateException("Cannot override the entire authorizeHttpRequests DSL and try to customize it at the same time.")
    }
    if (overrideAuthorizeExchange && customizeAuthorization) {
      throw IllegalStateException("Cannot override the entire authorizeExchange DSL and try to customize it at the same time.")
    }
    if (overrideAuthorizeHttpRequests && overrideAuthorizeExchange) {
      throw IllegalStateException("Cannot override both the authorizeHttpRequests DSL and authorizeExchange DSL as a Spring application cannot be both Servlet and Reactive based.")
    }
  }
}
