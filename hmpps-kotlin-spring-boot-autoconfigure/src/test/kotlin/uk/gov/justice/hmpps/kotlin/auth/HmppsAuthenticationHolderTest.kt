package uk.gov.justice.hmpps.kotlin.auth

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.mock
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.context.TestSecurityContextHolder
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners
import org.springframework.security.test.context.support.ReactorContextTestExecutionListener
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.hmpps.kotlin.auth.AuthSource.NONE
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder.Companion.hasRoles

@ExtendWith(SpringExtension::class)
@SecurityTestExecutionListeners
class HmppsAuthenticationHolderTest {
  private val holder = HmppsAuthenticationHolder()

  @AfterEach
  fun tearDown(): Unit = runTest {
    TestSecurityContextHolder.clearContext()
  }

  @Test
  fun `should throw exception if not a AuthAwareAuthenticationToken`() {
    TestSecurityContextHolder.setAuthentication(TestingAuthenticationToken("user", "pass"))
    ReactorContextTestExecutionListener().beforeTestMethod(null)

    assertThrows<InsufficientAuthenticationException> { holder.authentication }
  }

  @Test
  fun `should throw exception if no token found`() {
    assertThrows<AuthenticationCredentialsNotFoundException> { holder.authentication }
  }

  @Test
  fun `authenticationOrNull should return null if not a AuthAwareAuthenticationToken`() {
    TestSecurityContextHolder.setAuthentication(TestingAuthenticationToken("user", "pass"))
    ReactorContextTestExecutionListener().beforeTestMethod(null)

    assertThat(holder.authenticationOrNull).isNull()
  }

  @Test
  fun `authenticationOrNull should return null if no token found`() {
    assertThat(holder.authenticationOrNull).isNull()
  }

  @Test
  fun `authenticationOrNull should return value if set`() {
    setAuthentication(username = "some user name")

    assertThat(holder.authenticationOrNull?.principal).isEqualTo("some user name")
  }

  @Test
  fun `should take user_name as principal`() {
    setAuthentication(username = "some user name")

    assertThat(holder.principal).isEqualTo("some user name")
  }

  @Test
  fun `should fall back on client_id for principal`() {
    setAuthentication()

    assertThat(holder.principal).isEqualTo("clientId")
  }

  @Test
  fun `should add granted authorities`() {
    setAuthentication(setOf("ROLE_SOME_ROLE", "SCOPE_SOME_SCOPE"))

    assertThat(holder.roles).containsExactlyInAnyOrder(
      SimpleGrantedAuthority("ROLE_SOME_ROLE"),
      SimpleGrantedAuthority("SCOPE_SOME_SCOPE"),
    )
  }

  @Test
  fun `should be system client credentials`() {
    setAuthentication()

    assertThat(holder.isClientOnly).isTrue()
  }

  @Test
  fun `should not be system client credentials`() {
    setAuthentication(username = "some user name")

    assertThat(holder.isClientOnly).isFalse()
  }

  @Test
  fun `should return clientId`() {
    setAuthentication()

    assertThat(holder.clientId).isEqualTo("clientId")
  }

  @Test
  fun `should return none if no auth source`() = runTest {
    setAuthentication()

    assertThat(holder.authSource).isEqualTo(NONE)
  }

  @Test
  fun `should return auth source`() = runTest {
    setAuthentication(authSource = AuthSource.NOMIS)

    assertThat(holder.authSource).isEqualTo(AuthSource.NOMIS)
  }

  @ParameterizedTest
  @CsvSource("ROLE_SYSTEM_USER,true", "SYSTEM_USER,true", "SYSTEMUSER,false")
  fun hasRolesTest(role: String, expected: Boolean) {
    setAuthentication(setOf("ROLE_SYSTEM_USER"), username = "joe")
    assertThat(hasRoles(role)).isEqualTo(expected)
    assertThat(holder.isOverrideRole(role)).isEqualTo(expected)
    assertThat(holder.isClientOnly).isFalse
  }

  @ParameterizedTest
  @CsvSource("ROLE_SYSTEM_USER,true", "SYSTEM_USER,true", "SYSTEMUSER,false")
  fun hasClientRolesTest(role: String, expected: Boolean) {
    setAuthentication(setOf("ROLE_SYSTEM_USER"))
    assertThat(hasRoles(role)).isEqualTo(expected)
    assertThat(holder.isOverrideRole(role)).isEqualTo(expected)
    assertThat(holder.isClientOnly).isTrue
  }

  @Test
  fun isOverrideRole_NoOverrideRoleSet() {
    setAuthentication()
    assertThat(holder.isOverrideRole()).isFalse()
  }

  @Test
  fun hasRoles_NoAllowedRoleSet() {
    setAuthentication()
    assertThat(hasRoles()).isFalse()
  }

  private fun setAuthentication(
    rolesSet: Set<String> = setOf(),
    username: String? = null,
    authSource: AuthSource = NONE,
  ) {
    AuthAwareAuthenticationToken(
      mock(Jwt::class.java),
      "clientId",
      username,
      authSource,
      rolesSet.map { SimpleGrantedAuthority(it) },
    ).apply {
      TestSecurityContextHolder.setAuthentication(this)
    }
  }
}
