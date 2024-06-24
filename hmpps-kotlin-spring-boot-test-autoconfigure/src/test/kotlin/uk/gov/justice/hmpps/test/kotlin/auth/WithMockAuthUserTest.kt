package uk.gov.justice.hmpps.test.kotlin.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareAuthenticationToken
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@ExtendWith(SpringExtension::class)
@EnableMethodSecurity(prePostEnabled = true)
@ContextConfiguration(classes = [WithMockAuthUserTest.TestService::class])
class WithMockAuthUserTest {
  @TestConfiguration
  class TestService {
    private val hmppsAuthenticationHolder = HmppsAuthenticationHolder()

    @PreAuthorize("authenticated")
    fun getToken(): AuthAwareAuthenticationToken = hmppsAuthenticationHolder.authentication
  }

  @Autowired
  private lateinit var testService: TestService

  @Test
  @WithMockAuthUser
  fun `test name is set by default to user`() {
    assertThat(testService.getToken().name).isEqualTo("user")
  }

  @Test
  @WithMockAuthUser("joe")
  fun `test name can be set by setting value`() {
    assertThat(testService.getToken().name).isEqualTo("joe")
  }

  @Test
  @WithMockAuthUser(value = "", clientId = "joe-client")
  fun `test name can be set by setting clientId`() {
    assertThat(testService.getToken().name).isEqualTo("joe-client")
  }

  @Test
  @WithMockAuthUser
  fun `test principal is set by default to user`() {
    assertThat(testService.getToken().principal).isEqualTo("user")
  }

  @Test
  @WithMockAuthUser("joe")
  fun `test principal can be set by setting value`() {
    assertThat(testService.getToken().principal).isEqualTo("joe")
  }

  @Test
  @WithMockAuthUser(username = "joe")
  fun `test principal can be set by setting username`() {
    assertThat(testService.getToken().principal).isEqualTo("joe")
  }

  @Test
  @WithMockAuthUser(value = "", clientId = "joe-client")
  fun `test principal can be set by setting clientId`() {
    assertThat(testService.getToken().principal).isEqualTo("joe-client")
  }

  @Test
  @WithMockAuthUser(username = "joe", clientId = "joe-client")
  fun `test principal defaults to username rather than clientId`() {
    assertThat(testService.getToken().principal).isEqualTo("joe")
  }

  @Test
  @WithMockAuthUser
  fun `test clientId default`() {
    assertThat(testService.getToken().clientId).isEqualTo("test-client-id")
  }

  @Test
  @WithMockAuthUser(clientId = "joe-client")
  fun `test client can be set by setting clientId`() {
    assertThat(testService.getToken().clientId).isEqualTo("joe-client")
  }

  @Test
  @WithMockAuthUser
  fun `test auth source defaults to NONE`() {
    assertThat(testService.getToken().authSource).isEqualTo(AuthSource.NONE)
  }

  @Test
  @WithMockAuthUser(authSource = AuthSource.NOMIS)
  fun `test auth source can be set`() {
    assertThat(testService.getToken().authSource).isEqualTo(AuthSource.NOMIS)
  }

  @Test
  @WithMockAuthUser
  fun `test defaults authorities to ROLE_USER`() {
    assertThat(testService.getToken().authorities.map { it.authority }).containsExactly("ROLE_USER")
  }

  @Test
  @WithMockAuthUser(authorities = ["ROLE_BOB", "ROLE_JOE"])
  fun `test authorities can be set`() {
    assertThat(testService.getToken().authorities.map { it.authority }).containsExactly("ROLE_BOB", "ROLE_JOE")
  }

  @Test
  @WithMockAuthUser(roles = ["BOB", "JOE"])
  fun `test roles can be set`() {
    assertThat(testService.getToken().authorities.map { it.authority }).containsExactly("ROLE_BOB", "ROLE_JOE")
  }
}
