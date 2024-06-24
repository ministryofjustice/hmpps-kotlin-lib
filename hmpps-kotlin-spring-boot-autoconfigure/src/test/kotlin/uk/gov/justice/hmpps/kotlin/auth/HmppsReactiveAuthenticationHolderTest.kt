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
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.context.TestSecurityContextHolder
import org.springframework.security.test.context.support.ReactorContextTestExecutionListener
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.hmpps.kotlin.auth.AuthSource.NONE
import uk.gov.justice.hmpps.kotlin.auth.HmppsReactiveAuthenticationHolder.Companion.hasRoles

@ExtendWith(SpringExtension::class)
@TestExecutionListeners(ReactorContextTestExecutionListener::class)
class HmppsReactiveAuthenticationHolderTest {
  private val holder = HmppsReactiveAuthenticationHolder()

  @AfterEach
  fun tearDown(): Unit = runTest {
    TestSecurityContextHolder.clearContext()
  }

  @Test
  fun `should throw exception if not a AuthAwareAuthenticationToken`() = runTest {
    TestSecurityContextHolder.setAuthentication(TestingAuthenticationToken("user", "pass"))
    ReactorContextTestExecutionListener().beforeTestMethod(null)

    assertThrows<InsufficientAuthenticationException> { holder.getAuthentication() }
  }

  @Test
  fun `should take user_name as principal`() = runTest {
    setAuthentication(username = "some user name")

    assertThat(holder.getPrincipal()).isEqualTo("some user name")
  }

  @Test
  fun `should fall back on client_id for principal`() = runTest {
    setAuthentication()

    assertThat(holder.getPrincipal()).isEqualTo("clientId")
  }

  @Test
  fun `should add granted authorities`() = runTest {
    setAuthentication(setOf("ROLE_SOME_ROLE", "SCOPE_SOME_SCOPE"))

    assertThat(holder.getRoles()).containsExactlyInAnyOrder(
      SimpleGrantedAuthority("ROLE_SOME_ROLE"),
      SimpleGrantedAuthority("SCOPE_SOME_SCOPE"),
    )
  }

  @Test
  fun `should be system client credentials`() = runTest {
    setAuthentication()

    assertThat(holder.isClientOnly()).isTrue()
  }

  @Test
  fun `should not be system client credentials`() = runTest {
    setAuthentication(username = "some user name")

    assertThat(holder.isClientOnly()).isFalse()
  }

  @Test
  fun `should return clientId`() = runTest {
    setAuthentication()

    assertThat(holder.getClientId()).isEqualTo("clientId")
  }

  @ParameterizedTest
  @CsvSource("ROLE_SYSTEM_USER,true", "SYSTEM_USER,true", "SYSTEMUSER,false")
  fun hasRolesTest(role: String, expected: Boolean) = runTest {
    setAuthentication(setOf("ROLE_SYSTEM_USER"), username = "joe")
    assertThat(hasRoles(role)).isEqualTo(expected)
    assertThat(holder.isOverrideRole(role)).isEqualTo(expected)
    assertThat(holder.isClientOnly()).isFalse
  }

  @ParameterizedTest
  @CsvSource("ROLE_SYSTEM_USER,true", "SYSTEM_USER,true", "SYSTEMUSER,false")
  fun hasClientRolesTest(role: String, expected: Boolean) = runTest {
    setAuthentication(setOf("ROLE_SYSTEM_USER"))
    assertThat(hasRoles(role)).isEqualTo(expected)
    assertThat(holder.isOverrideRole(role)).isEqualTo(expected)
    assertThat(holder.isClientOnly()).isTrue
  }

  @Test
  fun isOverrideRole_NoOverrideRoleSet() = runTest {
    setAuthentication()
    assertThat(holder.isOverrideRole()).isFalse()
  }

  @Test
  fun hasRoles_NoAllowedRoleSet() = runTest {
    setAuthentication()
    assertThat(hasRoles()).isFalse()
  }

  private fun setAuthentication(rolesSet: Set<String> = setOf(), username: String? = null) = runTest {
    AuthAwareAuthenticationToken(
      mock(Jwt::class.java),
      "clientId",
      username,
      NONE,
      rolesSet.map { SimpleGrantedAuthority(it) },
    ).apply {
      TestSecurityContextHolder.setAuthentication(this)
    }
    ReactorContextTestExecutionListener().beforeTestMethod(null)
  }
}
