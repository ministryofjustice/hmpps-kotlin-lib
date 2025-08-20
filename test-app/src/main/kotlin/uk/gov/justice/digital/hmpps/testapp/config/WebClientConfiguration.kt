package uk.gov.justice.digital.hmpps.testapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import uk.gov.justice.hmpps.kotlin.auth.usernameAwareTokenRequestOAuth2AuthorizedClientManager
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.prison-api}") val prisonApiBaseUri: String,
  @Value("\${api.base.url.hmpps-auth}") val hmppsAuthBaseUri: String,
  @Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
  @Value("\${api.timeout:20s}") val timeout: Duration,
) {
  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)

  @Bean
  fun prisonApiHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(prisonApiBaseUri, healthTimeout)

  /**
   * Unless overridden the injected [org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager]
   * for this [org.springframework.web.reactive.function.client.WebClient] will use the
   * [uk.gov.justice.hmpps.kotlin.auth.service.GlobalPrincipalOAuth2AuthorizedClientService]
   * which caches all OAuth2 client credentials tokens under a single **principal**.
   */
  @Bean
  fun prisonApiWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient = builder.authorisedWebClient(
    authorizedClientManager,
    registrationId = "prison-api",
    url = prisonApiBaseUri,
    timeout,
  )

  /**
   * This [org.springframework.web.reactive.function.client.WebClient] constructs a custom
   * [org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager] for each request using
   * the [uk.gov.justice.hmpps.kotlin.auth.usernameAwareTokenRequestOAuth2AuthorizedClientManager] method.
   *
   * The [org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager] is configured to extract
   * the authenticated principal on each request and inject it as the **username** parameter in the
   * token request call.
   */
  @Bean
  @RequestScope
  fun usernameAwarePrisonApiWebClient(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
    builder: WebClient.Builder,
  ): WebClient = builder.authorisedWebClient(
    usernameAwareTokenRequestOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      oAuth2AuthorizedClientService,
      timeout,
    ),
    registrationId = "prison-api",
    url = prisonApiBaseUri,
    timeout,
  )
}
