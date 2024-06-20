package uk.gov.justice.hmpps.kotlin.auth

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class HmppsAuthenticationHolder {
  /**
   * This will return null if the token hasn't come from HMPPS Auth.  This is fine for application code, but tests need to
   * then use @WithMockAuthUser rather than using a TestingAuthenticationToken or @WithMockUser annotation.
   */
  val authentication: AuthAwareAuthenticationToken?
    get() = SecurityContextHolder.getContext().authentication as? AuthAwareAuthenticationToken

  /**
   * This is nullable since this can be called from an unprotected endpoint, but in the majority of cases it should
   * be not null.  This gets the current username from the authentication, falling back to the clientId if there
   * isn't a username passed in.
   */
  val currentPrincipal: String?
    get() = authentication?.principal

  val currentRoles: Collection<GrantedAuthority?>?
    get() = authentication?.authorities

  val isClientOnly: Boolean
    get() = authentication?.isSystemClientCredentials() ?: false

  val clientId: String?
    get() = authentication?.clientId

  fun isOverrideRole(vararg overrideRoles: String): Boolean =
    hasMatchingRole(getRoles(*overrideRoles), authentication)

  companion object {
    fun hasRoles(vararg allowedRoles: String): Boolean =
      hasMatchingRole(getRoles(*allowedRoles), SecurityContextHolder.getContext().authentication)

    private fun hasMatchingRole(roles: List<String>, authentication: Authentication?): Boolean =
      authentication?.authorities?.any { roles.contains(it?.authority?.replaceFirst("ROLE_", "")) }
        ?: false

    private fun getRoles(vararg roles: String): List<String> = roles.map { it.replaceFirst("ROLE_", "") }
  }
}
