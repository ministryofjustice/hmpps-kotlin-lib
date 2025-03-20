package uk.gov.justice.hmpps.kotlin.auth

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
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
   * This will throw an exception if the token is missing or hasn't come from HMPPS Auth.
   * If there is a possibility that the authentication won't be set (e.g. in event listeners or batch jobs) then
   * the authenticationOrNull is more suitable as it won't throw an exception but return null instead.
   *
   * Tests need to then use @WithMockAuthUser rather than using a TestingAuthenticationToken or @WithMockUser
   * annotation otherwise the exception will then be thrown.
   *
   * @throws NoSuchElementException if no security context set
   * @throws AuthenticationCredentialsNotFoundException if no authentication in security context
   * @throws InsufficientAuthenticationException if authentication not an AuthAwareAuthenticationToken
   */
  suspend fun getAuthentication(): AuthAwareAuthenticationToken = with(ReactiveSecurityContextHolder.getContext().awaitSingle().authentication) {
    if (this is AuthAwareAuthenticationToken) {
      return this
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
  suspend fun getAuthenticationOrNull(): AuthAwareAuthenticationToken? = ReactiveSecurityContextHolder.getContext().awaitSingleOrNull()?.authentication as? AuthAwareAuthenticationToken

  /**
   * This gets the current username from the authentication, falling back to the clientId if there isn't a username
   * passed in.
   *
   * @throws NoSuchElementException if no security context set
   * @throws AuthenticationCredentialsNotFoundException if no authentication in security context
   * @throws InsufficientAuthenticationException if authentication not an AuthAwareAuthenticationToken
   */
  suspend fun getPrincipal(): String = getAuthentication().principal

  /**
   * This will be null if there is no username in the token, only a clientId.  Use getPrincipal() to default to the
   * clientId if the username isn't set.
   *
   * @throws NoSuchElementException if no security context set
   * @throws AuthenticationCredentialsNotFoundException if no authentication in security context
   * @throws InsufficientAuthenticationException if authentication not an AuthAwareAuthenticationToken
   */
  suspend fun getUsername(): String? = getAuthentication().userName

  suspend fun getRoles(): Collection<GrantedAuthority?> = getAuthentication().authorities

  suspend fun isClientOnly(): Boolean = getAuthentication().userName == null

  suspend fun getClientId(): String = getAuthentication().clientId

  /**
   * We are gradually moving away from authorisation code tokens and instead using client credentials more often.
   * This property will be only set to something other than NONE for authorisation code tokens. For client credentials
   * tokens this will be NONE, even if a NOMIS or Delius username is passed in when creating the token.
   */
  suspend fun getAuthSource(): AuthSource = getAuthentication().authSource

  suspend fun isOverrideRole(vararg overrideRoles: String): Boolean = hasMatchingRole(getRoles(*overrideRoles), getAuthentication())

  companion object {
    suspend fun hasRoles(vararg allowedRoles: String): Boolean = hasMatchingRole(getRoles(*allowedRoles), ReactiveSecurityContextHolder.getContext().awaitSingle().authentication)

    private fun hasMatchingRole(roles: List<String>, authentication: Authentication?): Boolean = authentication?.authorities?.any { roles.contains(it?.authority?.replaceFirst("ROLE_", "")) }
      ?: false

    private fun getRoles(vararg roles: String): List<String> = roles.map { it.replaceFirst("ROLE_", "") }
  }
}
