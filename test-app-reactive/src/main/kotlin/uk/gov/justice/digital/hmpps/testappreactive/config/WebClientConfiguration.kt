package uk.gov.justice.digital.hmpps.testappreactive.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.reactiveAuthorisedWebClient
import uk.gov.justice.hmpps.kotlin.auth.reactiveHealthWebClient
import uk.gov.justice.hmpps.kotlin.auth.reactiveUsernameAwareTokenRequestOAuth2AuthorizedClientManager
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.prison-api}") val prisonApiBaseUri: String,
  @Value("\${api.base.url.hmpps-auth}") val hmppsAuthBaseUri: String,
  @Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
  @Value("\${api.timeout:20s}") val timeout: Duration,
) {
  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.reactiveHealthWebClient(hmppsAuthBaseUri, healthTimeout)

  @Bean
  fun prisonApiHealthWebClient(builder: WebClient.Builder): WebClient = builder.reactiveHealthWebClient(prisonApiBaseUri, healthTimeout)

  @Bean
  fun prisonApiWebClient(reactiveAuthorizedClientManager: ReactiveOAuth2AuthorizedClientManager, builder: WebClient.Builder): WebClient = builder.reactiveAuthorisedWebClient(reactiveAuthorizedClientManager, registrationId = "prison-api", url = prisonApiBaseUri, timeout)

  /**
   * This [org.springframework.web.reactive.function.client.WebClient] uses a
   * [org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager] configured with an exchange
   * filter function to extract the authenticated principal on each request and inject it as the **username** parameter in the
   * token request call.
   */
  @Bean
  fun usernameAwarePrisonApiWebClient(
    reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
    reactiveOAuth2AuthorizedClientService: ReactiveOAuth2AuthorizedClientService,
    builder: WebClient.Builder,
  ): WebClient = builder.reactiveAuthorisedWebClient(
    reactiveUsernameAwareTokenRequestOAuth2AuthorizedClientManager(
      reactiveClientRegistrationRepository,
      reactiveOAuth2AuthorizedClientService,
      timeout,
    ),
    registrationId = "prison-api",
    url = prisonApiBaseUri,
    timeout,
  )
}
