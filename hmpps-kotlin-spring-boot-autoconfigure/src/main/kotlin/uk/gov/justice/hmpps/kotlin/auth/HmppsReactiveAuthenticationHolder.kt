package uk.gov.justice.hmpps.kotlin.auth

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component

@Component
@ConditionalOnWebApplication(type = REACTIVE)
class HmppsReactiveAuthenticationHolder {
  /**
   * This will return null if the token hasn't come from HMPPS Auth.  This is fine for application code, but tests need to
   * then use @WithMockAuthUser rather than using a TestingAuthenticationToken or @WithMockUser annotation.
   */
  suspend fun getAuthentication(): AuthAwareAuthenticationToken =
    with(ReactiveSecurityContextHolder.getContext().awaitSingle().authentication) {
      if (this is AuthAwareAuthenticationToken) {
        return this
      } else if (this == null) {
        throw AuthenticationCredentialsNotFoundException("No credentials found")
      } else {
        throw InsufficientAuthenticationException("Authentication not an instance of AuthAwareAuthenticationToken, found $this instead")
      }
    }

  /**
   * This gets the current username from the authentication, falling back to the clientId if thereisn't a username
   * passed in.
   */
  suspend fun getPrincipal(): String = getAuthentication().principal

  /**
   * This will be null if there is no username in the token, only a clientId.  Use getPrincipal() to default to the
   * clientId if the username isn't set.
   */
  suspend fun getUsername(): String? = getAuthentication().userName

  suspend fun getRoles(): Collection<GrantedAuthority?> = getAuthentication().authorities

  suspend fun isClientOnly(): Boolean = getAuthentication().isSystemClientCredentials() ?: false

  suspend fun getClientId(): String = getAuthentication().clientId

  suspend fun isOverrideRole(vararg overrideRoles: String): Boolean =
    hasMatchingRole(getRoles(*overrideRoles), getAuthentication())

  companion object {
    suspend fun hasRoles(vararg allowedRoles: String): Boolean =
      hasMatchingRole(getRoles(*allowedRoles), ReactiveSecurityContextHolder.getContext().awaitSingle().authentication)

    private fun hasMatchingRole(roles: List<String>, authentication: Authentication?): Boolean =
      authentication?.authorities?.any { roles.contains(it?.authority?.replaceFirst("ROLE_", "")) }
        ?: false

    private fun getRoles(vararg roles: String): List<String> = roles.map { it.replaceFirst("ROLE_", "") }
  }
}
