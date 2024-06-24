package uk.gov.justice.hmpps.kotlin.auth

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
@ConditionalOnWebApplication(type = SERVLET)
class HmppsAuthenticationHolder {
  /**
   * This will return null if the token hasn't come from HMPPS Auth.  This is fine for application code, but tests need to
   * then use @WithMockAuthUser rather than using a TestingAuthenticationToken or @WithMockUser annotation.
   */
  val authentication: AuthAwareAuthenticationToken
    get() = with(SecurityContextHolder.getContext().authentication) {
      if (this is AuthAwareAuthenticationToken) {
        return this
      } else if (this == null) {
        throw AuthenticationCredentialsNotFoundException("No credentials found")
      } else {
        throw InsufficientAuthenticationException("Authentication not an instance of AuthAwareAuthenticationToken, found $this instead")
      }
    }

  /**
   * This gets the current username from the authentication, falling back to the clientId if there isn't a username
   * passed in.
   */
  val principal: String
    get() = authentication.principal

  /**
   * This will be null if there is no username in the token, only a clientId.  Use principal to default to the clientId
   * if the username isn't set.
   */
  val username: String?
    get() = authentication.userName

  val roles: Collection<GrantedAuthority?>
    get() = authentication.authorities

  val isClientOnly: Boolean
    get() = authentication.isSystemClientCredentials()

  val clientId: String
    get() = authentication.clientId

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
