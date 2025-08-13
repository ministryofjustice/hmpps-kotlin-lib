package uk.gov.justice.hmpps.kotlin.auth.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService

class GlobalPrincipalOAuth2AuthorizedClientServiceTest {

  companion object {
    const val REGISTRATION_ID = "test-client"
    const val SERVICE_TO_SERVICE_PRINCIPAL_NAME = "global-service-to-service-principal"
    const val PRINCIPAL_TO_OVERRIDE = "principal-to-override"
  }

  private val grantedAuthorities = listOf(SimpleGrantedAuthority("ABC123"))

  private val serviceToServicePrincipal = UsernamePasswordAuthenticationToken(SERVICE_TO_SERVICE_PRINCIPAL_NAME, "")

  private var oAuth2AuthorizedClientMock: OAuth2AuthorizedClient = mock()
  private var principalMock: Authentication = mock()
  private var wrappedService: OAuth2AuthorizedClientService = mock()
  private lateinit var service: OAuth2AuthorizedClientService

  @BeforeEach
  fun setup() {
    service = GlobalPrincipalOAuth2AuthorizedClientService(wrappedService)

    whenever(principalMock.name)
      .thenReturn(PRINCIPAL_TO_OVERRIDE)

    whenever(principalMock.authorities)
      .thenReturn(grantedAuthorities)

    whenever(
      wrappedService.loadAuthorizedClient(
        REGISTRATION_ID,
        SERVICE_TO_SERVICE_PRINCIPAL_NAME,
      ) as OAuth2AuthorizedClient?,
    ).thenReturn(oAuth2AuthorizedClientMock)
  }

  @Test
  fun `loadAuthorizedClient success the expected auth client is returned `() {
    val result: OAuth2AuthorizedClient? =
      service.loadAuthorizedClient(
        REGISTRATION_ID,
        PRINCIPAL_TO_OVERRIDE,
      )
    assertThat(result).isNotNull()

    assertThat(result).isEqualTo(oAuth2AuthorizedClientMock)

    verify(wrappedService, times(1))
      .loadAuthorizedClient(
        REGISTRATION_ID,
        SERVICE_TO_SERVICE_PRINCIPAL_NAME,
      ) as OAuth2AuthorizedClient?
  }

  @Test
  fun `loadAuthorizedClient loads clients using the single principal name instead of provided principal name value`() {
    val result: OAuth2AuthorizedClient? = service.loadAuthorizedClient(REGISTRATION_ID, PRINCIPAL_TO_OVERRIDE)
    assertThat(result).isNotNull()

    assertThat(result).isEqualTo(oAuth2AuthorizedClientMock)

    verify(wrappedService, times(1))
      .loadAuthorizedClient(
        REGISTRATION_ID,
        SERVICE_TO_SERVICE_PRINCIPAL_NAME,
      ) as OAuth2AuthorizedClient?
  }

  @Test
  fun `saveAuthorizedClient uses the expected service to service principal`() {
    service.saveAuthorizedClient(oAuth2AuthorizedClientMock, principalMock)

    verify(wrappedService, times(1))
      .saveAuthorizedClient(eq(oAuth2AuthorizedClientMock), eq(serviceToServicePrincipal))
  }

  @Test
  fun `removeAuthorizedClient propagates exception thrown by wrapped OAuth2AuthorizedClientService`() {
    whenever(wrappedService.removeAuthorizedClient(any(), any()))
      .thenThrow(RuntimeException::class.java)

    assertThatThrownBy {
      service.removeAuthorizedClient(REGISTRATION_ID, PRINCIPAL_TO_OVERRIDE)
    }

    verify(wrappedService, times(1))
      .removeAuthorizedClient(
        REGISTRATION_ID,
        SERVICE_TO_SERVICE_PRINCIPAL_NAME,
      )
  }

  @Test
  fun `removeAuthorizedClient uses expected principal regardless of input principal`() {
    service.removeAuthorizedClient(REGISTRATION_ID, PRINCIPAL_TO_OVERRIDE)

    verify(wrappedService, times(1))
      .removeAuthorizedClient(
        REGISTRATION_ID,
        SERVICE_TO_SERVICE_PRINCIPAL_NAME,
      )
  }
}
