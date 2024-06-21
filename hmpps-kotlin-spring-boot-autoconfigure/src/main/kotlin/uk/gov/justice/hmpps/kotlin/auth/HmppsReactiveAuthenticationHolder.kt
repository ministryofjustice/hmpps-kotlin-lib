package uk.gov.justice.hmpps.kotlin.auth

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component

@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class HmppsReactiveAuthenticationHolder {
  /**
   * This will return null if the token hasn't come from HMPPS Auth.  This is fine for application code, but tests need to
   * then use @WithMockAuthUser rather than using a TestingAuthenticationToken or @WithMockUser annotation.
   */
  suspend fun getAuthentication(): AuthAwareAuthenticationToken? =
    ReactiveSecurityContextHolder.getContext().awaitSingle().authentication as? AuthAwareAuthenticationToken

  /**
   * This is nullable since this can be called from an unprotected endpoint, but in the majority of cases it should
   * be not null.  This gets the current username from the authentication, falling back to the clientId if there
   * isn't a username passed in.
   */
  suspend fun getPrincipal(): String? = getAuthentication()?.principal

  suspend fun getRoles(): Collection<GrantedAuthority?>? = getAuthentication()?.authorities

  suspend fun isClientOnly(): Boolean = getAuthentication()?.isSystemClientCredentials() ?: false

  suspend fun getClientId(): String? = getAuthentication()?.clientId

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
