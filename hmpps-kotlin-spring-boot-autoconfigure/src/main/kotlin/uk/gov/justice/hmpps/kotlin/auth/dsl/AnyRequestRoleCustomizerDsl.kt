package uk.gov.justice.hmpps.kotlin.auth.dsl

@DslMarker
annotation class AnyRequestRoleCustomizerDslMarker

/**
 * Part of the [ResourceServerConfigurationCustomizerDsl] DSL.
 *
 * To create a new instance of [AnyRequestRoleCustomizer], use the [ResourceServerConfigurationCustomizer.Companion.invoke] method, e.g.
 *
 * ```
 *   @Bean
 *   fun resourceServerCustomizer() = ResourceServerConfigurationCustomizer {
 *     anyRequestRole { defaultRole("ROLE_MY_ROLE") }
 *   }
 * ```
 */
@AnyRequestRoleCustomizerDslMarker
interface AnyRequestRoleCustomizerDsl {
  /**
   * Sets the role required for any unauthorized request. If not provided then no role will be required, just a valid token.
   *
   * Note that this doesn't override the @PreAuthorize annotation, only the default role protection for anyRequest.
   */
  var defaultRole: String?
}

class AnyRequestRoleCustomizer(
  val defaultRole: String? = null,
)

class AnyRequestRoleCustomizerBuilder : AnyRequestRoleCustomizerDsl {
  override var defaultRole: String? = null

  fun build(): AnyRequestRoleCustomizer = AnyRequestRoleCustomizer(defaultRole)
}
