package uk.gov.justice.hmpps.kotlin.auth.service

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientId
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

class GlobalPrincipalReactiveOAuth2AuthorizedClientService(
  private val clientRegistrationRepository: ReactiveClientRegistrationRepository,
) : ReactiveOAuth2AuthorizedClientService {

  companion object {
    const val GLOBAL_SYSTEM_PRINCIPAL = "global-system-principal"
  }

  private val authorizedClients: MutableMap<OAuth2AuthorizedClientId, OAuth2AuthorizedClient> =
    ConcurrentHashMap()

  override fun <T : OAuth2AuthorizedClient?> loadAuthorizedClient(
    clientRegistrationId: String?,
    principalName: String?,
  ): Mono<T?> = Mono.justOrEmpty(clientRegistrationId)
    .flatMap { id -> clientRegistrationRepository.findByRegistrationId(id) }
    .mapNotNull { clientRegistration ->
      @Suppress("UNCHECKED_CAST")
      authorizedClients[OAuth2AuthorizedClientId(clientRegistration.registrationId, GLOBAL_SYSTEM_PRINCIPAL)] as? T
    }

  override fun saveAuthorizedClient(authorizedClient: OAuth2AuthorizedClient, principal: Authentication?): Mono<Void> {
    authorizedClients[
      OAuth2AuthorizedClientId(authorizedClient.clientRegistration.registrationId, GLOBAL_SYSTEM_PRINCIPAL),
    ] = authorizedClient
    return Mono.empty()
  }

  override fun removeAuthorizedClient(clientRegistrationId: String?, principalName: String?): Mono<Void> = Mono.justOrEmpty(clientRegistrationId)
    .flatMap { id -> clientRegistrationRepository.findByRegistrationId(id) }
    .doOnNext { registration ->
      authorizedClients.remove(OAuth2AuthorizedClientId(registration.registrationId, GLOBAL_SYSTEM_PRINCIPAL))
    }
    .then()
}
