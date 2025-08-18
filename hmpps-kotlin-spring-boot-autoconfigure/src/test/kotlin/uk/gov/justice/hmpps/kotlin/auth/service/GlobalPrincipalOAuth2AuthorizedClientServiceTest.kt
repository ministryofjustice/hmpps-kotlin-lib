package uk.gov.justice.hmpps.kotlin.auth.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import java.time.Duration
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class GlobalPrincipalOAuth2AuthorizedClientServiceTest {

  companion object {
    const val TEST_REGISTRATION_ID = "test-service-client"
    const val TEST_SYSTEM_USERNAME = "test-service"
    const val TEST_PRINCIPAL_ONE = "principal-one"
    const val TEST_PRINCIPAL_TWO = "principal-two"
    val TEST_CLIENT_REGISTRATION: ClientRegistration =
      ClientRegistration.withRegistrationId(TEST_REGISTRATION_ID)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .clientId("clientId")
        .clientSecret("clientSecret")
        .tokenUri("tokenUri")
        .build()
    val TEST_CLIENT_TOKEN = OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "test-token", Instant.now(), Instant.now().plus(Duration.ofHours(1)))
    val TEST_AUTHORIZED_CLIENT = OAuth2AuthorizedClient(TEST_CLIENT_REGISTRATION, TEST_SYSTEM_USERNAME, TEST_CLIENT_TOKEN)
    val AUTHENTICATED_PRINCIPAL_ONE = UsernamePasswordAuthenticationToken(TEST_PRINCIPAL_ONE, null)
  }

  @Mock
  private lateinit var clientRegistrationRepositoryMock: ClientRegistrationRepository

  private lateinit var globalPrincipalOAuth2AuthorizedClientService: GlobalPrincipalOAuth2AuthorizedClientService

  @BeforeEach
  fun setup() {
    globalPrincipalOAuth2AuthorizedClientService = GlobalPrincipalOAuth2AuthorizedClientService(clientRegistrationRepositoryMock)
  }

  @Nested
  inner class LoadAuthorizedClientTests {

    @Test
    fun `loadAuthorizedClient returns the expected cached OAuth2AuthorizedClient for a given registration id even if the authenticated principal is different`() {
      whenever(clientRegistrationRepositoryMock.findByRegistrationId(TEST_REGISTRATION_ID))
        .thenReturn(TEST_CLIENT_REGISTRATION)
      globalPrincipalOAuth2AuthorizedClientService.saveAuthorizedClient(TEST_AUTHORIZED_CLIENT, AUTHENTICATED_PRINCIPAL_ONE)

      assertCachedAuthorizedClientsStateIsCorrect(listOf(TEST_PRINCIPAL_ONE, TEST_PRINCIPAL_TWO), TEST_AUTHORIZED_CLIENT)

      verify(clientRegistrationRepositoryMock, times(2))
        .findByRegistrationId(TEST_REGISTRATION_ID)
    }

    @Test
    fun `loadAuthorizedClient returns null if the requested registration id is not found in the registered clients repository`() {
      whenever(clientRegistrationRepositoryMock.findByRegistrationId(TEST_REGISTRATION_ID))
        .thenReturn(null)

      assertCachedAuthorizedClientsStateIsCorrect(listOf(TEST_PRINCIPAL_ONE, TEST_PRINCIPAL_TWO), null)

      verify(clientRegistrationRepositoryMock, times(2))
        .findByRegistrationId(TEST_REGISTRATION_ID)
    }

    @Test
    fun `loadAuthorizedClient returns null if the requested OAuth2AuthorizedClient has not been cached under the system username`() {
      whenever(clientRegistrationRepositoryMock.findByRegistrationId(TEST_REGISTRATION_ID))
        .thenReturn(TEST_CLIENT_REGISTRATION)

      assertCachedAuthorizedClientsStateIsCorrect(listOf(TEST_PRINCIPAL_ONE, TEST_PRINCIPAL_TWO), null)

      verify(clientRegistrationRepositoryMock, times(2))
        .findByRegistrationId(TEST_REGISTRATION_ID)
    }
  }

  @Nested
  inner class SaveAuthorizedClientTests {

    @Test
    fun `saveAuthorizedClient stores OAuth2AuthorizedClient under the system username instead of the authenticated principal`() {
      whenever(clientRegistrationRepositoryMock.findByRegistrationId(TEST_REGISTRATION_ID))
        .thenReturn(TEST_CLIENT_REGISTRATION)

      globalPrincipalOAuth2AuthorizedClientService.saveAuthorizedClient(TEST_AUTHORIZED_CLIENT, AUTHENTICATED_PRINCIPAL_ONE)

      assertCachedAuthorizedClientsStateIsCorrect(listOf(TEST_PRINCIPAL_ONE, TEST_PRINCIPAL_TWO), TEST_AUTHORIZED_CLIENT)
    }
  }

  @Nested
  inner class RemoveAuthorizedClientTests {
    @BeforeEach
    fun setup() {
      whenever(clientRegistrationRepositoryMock.findByRegistrationId(TEST_REGISTRATION_ID))
        .thenReturn(TEST_CLIENT_REGISTRATION)

      globalPrincipalOAuth2AuthorizedClientService.saveAuthorizedClient(TEST_AUTHORIZED_CLIENT, AUTHENTICATED_PRINCIPAL_ONE)
    }

    @Test
    fun `removeAuthorizedClient removes an OAuth2AuthorizedClient cached by a different authenticated principal`() {
      assertCachedAuthorizedClientsStateIsCorrect(listOf(TEST_PRINCIPAL_ONE), TEST_AUTHORIZED_CLIENT)

      globalPrincipalOAuth2AuthorizedClientService.removeAuthorizedClient(TEST_REGISTRATION_ID, TEST_PRINCIPAL_TWO)

      assertCachedAuthorizedClientsStateIsCorrect(listOf(TEST_PRINCIPAL_ONE, TEST_PRINCIPAL_TWO), null)

      verify(clientRegistrationRepositoryMock, times(4))
        .findByRegistrationId(TEST_REGISTRATION_ID)
    }

    @Test
    fun `removeAuthorizedClient removes an OAuth2AuthorizedClient cached by a same authenticated principal`() {
      assertCachedAuthorizedClientsStateIsCorrect(listOf(TEST_PRINCIPAL_ONE), TEST_AUTHORIZED_CLIENT)

      globalPrincipalOAuth2AuthorizedClientService.removeAuthorizedClient(TEST_REGISTRATION_ID, TEST_PRINCIPAL_ONE)

      assertCachedAuthorizedClientsStateIsCorrect(listOf(TEST_PRINCIPAL_ONE, TEST_PRINCIPAL_TWO), null)

      verify(clientRegistrationRepositoryMock, times(4))
        .findByRegistrationId(TEST_REGISTRATION_ID)
    }
  }

  private fun assertCachedAuthorizedClientsStateIsCorrect(
    testPrincipals: Collection<String>,
    expectedClient: OAuth2AuthorizedClient?,
  ) {
    for (testPrincipal in testPrincipals) {
      val returnedClient: OAuth2AuthorizedClient? = globalPrincipalOAuth2AuthorizedClientService.loadAuthorizedClient(
        TEST_REGISTRATION_ID,
        testPrincipal,
      )
      assertThat(returnedClient).isEqualTo(expectedClient)
    }
  }
}
