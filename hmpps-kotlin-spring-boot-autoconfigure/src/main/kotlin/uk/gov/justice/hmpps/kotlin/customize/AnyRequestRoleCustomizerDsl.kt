package uk.gov.justice.hmpps.kotlin.customize

@DslMarker
annotation class AnyRequestRoleCustomizerDslMarker

/**
 * Part of the [ResourceServerConfigurationCustomizerDsl] DSL.
 *
 * To create a new instance of [AnyRequestRoleCustomizer], use the [ResourceServerConfigurationCustomizer.Companion.build] method, e.g.
 *
 * <pre>
 *   @Bean
 *   fun configurationCustomizer() = ResourceServerConfigurationCustomizer.build {
 *     anyRequestRole { defaultRole("ROLE_MY_ROLE") }
 *   }
 * </pre>
 */
@AnyRequestRoleCustomizerDslMarker
interface AnyRequestRoleCustomizerDsl {
  /**
   * Sets the role required for any unauthorized request. If not provided then no role will be required, just a valid token.
   *
   * Note that this doesn't override the @PreAuthorize annotation, only the default role protection for anyRequest.
   */
  fun defaultRole(role: String): AnyRequestRoleCustomizerBuilder
}

class AnyRequestRoleCustomizer(
  val defaultRole: String? = null,
)

class AnyRequestRoleCustomizerBuilder : AnyRequestRoleCustomizerDsl {
  private var defaultRole: String? = null

  override fun defaultRole(role: String): AnyRequestRoleCustomizerBuilder =
    this.apply {
      defaultRole = role
    }

  fun build(): AnyRequestRoleCustomizer {
    return AnyRequestRoleCustomizer(defaultRole)
  }
}
