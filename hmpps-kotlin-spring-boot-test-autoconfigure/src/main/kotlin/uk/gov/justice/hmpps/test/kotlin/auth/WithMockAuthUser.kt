package uk.gov.justice.hmpps.test.kotlin.auth

import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import org.springframework.util.Assert
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareAuthenticationToken
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import java.lang.annotation.Inherited

/**
 * Similar to {@link org.springframework.security.test.context.support.WithMockUser} but
 * populates the security context with a {@link uk.gov.justice.hmpps.kotlin.auth.AuthAwareAuthenticationToken} instead.
 */
@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER,
  AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@WithSecurityContext(factory = WithMockUserSecurityContextFactory::class)
annotation class WithMockAuthUser(
  val value: String = "user",
  val username: String = "",
  val authorities: Array<String> = [],
  val roles: Array<String> = ["USER"],
  val authSource: AuthSource = AuthSource.NONE,
  val clientId: String = "test-client-id",
)

internal class WithMockUserSecurityContextFactory : WithSecurityContextFactory<WithMockAuthUser> {
  private var securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy()

  override fun createSecurityContext(withUser: WithMockAuthUser): SecurityContext {
    val username = withUser.username.ifEmpty { withUser.value }
    Assert.notNull(username) { "$withUser cannot have null username on both username and value properties" }

    val grantedAuthorities = if (withUser.authorities.isEmpty()) {
      withUser.roles.map { SimpleGrantedAuthority(if (it.startsWith("ROLE_")) it else "ROLE_$it") }
    } else {
      if (!(withUser.roles.size == 1 && "USER" == withUser.roles[0])) {
        throw IllegalStateException(
          "You cannot define roles attribute ${listOf(*withUser.roles)} with authorities attribute ${listOf(*withUser.authorities)}",
        )
      }
      withUser.authorities.map { SimpleGrantedAuthority(it) }
    }

    val authentication: Authentication = AuthAwareAuthenticationToken(
      jwt = Jwt.withTokenValue(
        JwtAuthorisationHelper().createJwtAccessToken(
          username = username,
          roles = grantedAuthorities.map { it.authority },
          clientId = withUser.clientId,
        ),
      ).header("head", "value").claim("claim", "value").build(),
      clientId = withUser.clientId,
      userName = username,
      authorities = grantedAuthorities,
      authSource = withUser.authSource,
    )
    return securityContextHolderStrategy.createEmptyContext().apply { this.authentication = authentication }
  }
}
