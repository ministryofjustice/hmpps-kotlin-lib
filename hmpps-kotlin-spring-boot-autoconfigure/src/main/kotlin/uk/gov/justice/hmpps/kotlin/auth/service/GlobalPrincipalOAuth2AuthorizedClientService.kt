package uk.gov.justice.hmpps.kotlin.auth.service

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService

/**
 * OAuth2AuthorizedClientService implementation that caches clients using the single provided principal name. Clients are
 * cached against a [org.springframework.security.oauth2.client.OAuth2AuthorizedClientId] key constructed from:
 * - **clientRegistrationId** And
 * - **principalName** - Hardcoded to `global-system-principal`.
 *
 * This class is designed to wrap the existing instance of the [org.springframework.security.oauth2.client.OAuth2AuthorizedClientService]
 * and override the **principalName**
 *
 * The default implementation [org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService] sets
 * the **principalName** to the **principal** extracted from the current [org.springframework.security.core.Authentication] object.
 * HMPPS digital service use the OAuth 2.0 client credentials grant for service-to-service calls, however HMPPS Auth provides the ability to embed
 * the username of user initiating the action in the system client token for additional context. When using the
 * [uk.gov.justice.hmpps.kotlin.auth.AuthAwareAuthenticationToken], if there is a username claim present in the token then
 * this will be used as the authenticated principal instead of the system client id. This means that without the global principal
 * a new token will be created per user session. During periods of heavy traffic this creates additional load on the Auth token endpoint.
 */
class GlobalPrincipalOAuth2AuthorizedClientService(
  private val wrappedOAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
) : OAuth2AuthorizedClientService {

  companion object {
    const val GLOBAL_PRINCIPAL = "global-system-principal"
    private val GLOBAL_PRINCIPAL_AUTHENTICATION_OBJECT = UsernamePasswordAuthenticationToken(GLOBAL_PRINCIPAL, "")
  }

  override fun <T : OAuth2AuthorizedClient?> loadAuthorizedClient(
    clientRegistrationId: String,
    principalName: String,
  ): T? = wrappedOAuth2AuthorizedClientService.loadAuthorizedClient(
    clientRegistrationId,
    GLOBAL_PRINCIPAL,
  )

  override fun saveAuthorizedClient(
    authorizedClient: OAuth2AuthorizedClient,
    principal: Authentication,
  ) = wrappedOAuth2AuthorizedClientService.saveAuthorizedClient(
    authorizedClient,
    GLOBAL_PRINCIPAL_AUTHENTICATION_OBJECT,
  )

  override fun removeAuthorizedClient(
    clientRegistrationId: String,
    principalName: String,
  ) = wrappedOAuth2AuthorizedClientService.removeAuthorizedClient(
    clientRegistrationId,
    GLOBAL_PRINCIPAL,
  )
}
