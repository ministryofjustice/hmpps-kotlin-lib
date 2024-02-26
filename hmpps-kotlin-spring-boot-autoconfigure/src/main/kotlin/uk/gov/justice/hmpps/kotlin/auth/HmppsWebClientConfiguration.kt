package uk.gov.justice.hmpps.kotlin.auth

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import kotlin.apply as kotlinApply

private const val DEFAULT_TIMEOUT_SECONDS: Long = 30
private const val DEFAULT_HEALTH_TIMEOUT_SECONDS: Long = 2

@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnBean(ClientRegistrationRepository::class)
@Configuration
class HmppsWebClientConfiguration {
  @ConditionalOnMissingBean
  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    return AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      oAuth2AuthorizedClientService,
    ).kotlinApply { setAuthorizedClientProvider(authorizedClientProvider) }
  }
}

@ConditionalOnWebApplication(type = REACTIVE)
@ConditionalOnBean(ReactiveClientRegistrationRepository::class)
@Configuration
class HmppsReactiveWebClientConfiguration {

  @ConditionalOnMissingBean
  @Bean
  fun reactiveAuthorizedClientManager(
    clientRegistrationRepository: ReactiveClientRegistrationRepository,
    oAuth2AuthorizedClientService: ReactiveOAuth2AuthorizedClientService,
  ): ReactiveOAuth2AuthorizedClientManager = AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
    clientRegistrationRepository,
    oAuth2AuthorizedClientService,
  ).kotlinApply { setAuthorizedClientProvider(ReactiveOAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()) }
}

fun WebClient.Builder.authorisedWebClient(
  authorizedClientManager: OAuth2AuthorizedClientManager,
  registrationId: String,
  url: String,
  timeout: Duration = Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS),
): WebClient {
  val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager).kotlinApply {
    setDefaultClientRegistrationId(registrationId)
  }

  return baseUrl(url)
    .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(timeout)))
    .filter(oauth2Client)
    .build()
}

fun WebClient.Builder.healthWebClient(
  url: String,
  healthTimeout: Duration = Duration.ofSeconds(DEFAULT_HEALTH_TIMEOUT_SECONDS),
): WebClient =
  baseUrl(url)
    .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(healthTimeout)))
    .build()

fun WebClient.Builder.reactiveAuthorisedWebClient(
  authorizedClientManager: ReactiveOAuth2AuthorizedClientManager,
  registrationId: String,
  url: String,
  timeout: Duration = Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS),
): WebClient =
  baseUrl(url)
    .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(timeout)))
    .filter(
      ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager).kotlinApply {
        setDefaultClientRegistrationId(registrationId)
      },
    )
    .build()

fun WebClient.Builder.reactiveHealthWebClient(
  url: String,
  healthTimeout: Duration = Duration.ofSeconds(DEFAULT_HEALTH_TIMEOUT_SECONDS),
): WebClient =
  baseUrl(url)
    .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(healthTimeout)))
    .build()
