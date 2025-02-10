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
   * This will throw an exception if the token is missing or hasn't come from HMPPS Auth.
   * If there is a possibility that the authentication won't be set (e.g. in event listeners or batch jobs) then
   * the authenticationOrNull is more suitable as it won't throw an exception but return null instead.
   *
   * Tests need to then use @WithMockAuthUser rather than using a TestingAuthenticationToken or @WithMockUser
   * annotation otherwise the exception will then be thrown.
   *
   * @throws AuthenticationCredentialsNotFoundException if no authentication in security context
   * @throws InsufficientAuthenticationException if authentication not an AuthAwareAuthenticationToken
   */
  val authentication: AuthAwareAuthenticationToken
    get() = with(SecurityContextHolder.getContext().authentication) {
      if (this is AuthAwareAuthenticationToken) {
        this
      } else if (this == null) {
        throw AuthenticationCredentialsNotFoundException("No credentials found")
      } else {
        throw InsufficientAuthenticationException("Authentication not an instance of AuthAwareAuthenticationToken, found $this instead")
      }
    }

  /**
   * This will return null if the token is missing or hasn't come from HMPPS Auth.
   * This will be the case for event listeners and batch jobs so is more suitable if that can be the case.
   */
  val authenticationOrNull: AuthAwareAuthenticationToken?
    get() = SecurityContextHolder.getContext().authentication as? AuthAwareAuthenticationToken

  /**
   * This gets the current username from the authentication, falling back to the clientId if there isn't a username
   * passed in.
   *
   * @throws AuthenticationCredentialsNotFoundException if no authentication in security context
   * @throws InsufficientAuthenticationException if authentication not an AuthAwareAuthenticationToken
   */
  val principal: String
    get() = authentication.principal

  /**
   * This will be null if there is no username in the token, only a clientId.  Use principal to default to the clientId
   * if the username isn't set.
   *
   * @throws AuthenticationCredentialsNotFoundException if no authentication in security context
   * @throws InsufficientAuthenticationException if authentication not an AuthAwareAuthenticationToken
   */
  val username: String?
    get() = authentication.userName

  val roles: Collection<GrantedAuthority?>
    get() = authentication.authorities

  val isClientOnly: Boolean
    get() = authentication.isSystemClientCredentials()

  val clientId: String
    get() = authentication.clientId

  /**
   * We are gradually moving away from authorisation code tokens and instead using client credentials more often.
   * This property will be only set to something other than NONE for authorisation code tokens. For client credentials
   * tokens this will be NONE, even if a NOMIS or Delius username is passed in when creating the token.
   */
  val authSource: AuthSource
    get() = authentication.authSource

  fun isOverrideRole(vararg overrideRoles: String): Boolean = hasMatchingRole(getRoles(*overrideRoles), authentication)

  companion object {
    fun hasRoles(vararg allowedRoles: String): Boolean = hasMatchingRole(getRoles(*allowedRoles), SecurityContextHolder.getContext().authentication)

    private fun hasMatchingRole(roles: List<String>, authentication: Authentication?): Boolean = authentication?.authorities?.any { roles.contains(it?.authority?.replaceFirst("ROLE_", "")) }
      ?: false

    private fun getRoles(vararg roles: String): List<String> = roles.map { it.replaceFirst("ROLE_", "") }
  }
}
