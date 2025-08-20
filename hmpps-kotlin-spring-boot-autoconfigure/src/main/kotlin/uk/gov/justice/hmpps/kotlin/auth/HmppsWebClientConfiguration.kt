package uk.gov.justice.hmpps.kotlin.auth

import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.hmpps.kotlin.auth.service.GlobalPrincipalOAuth2AuthorizedClientService
import uk.gov.justice.hmpps.kotlin.auth.service.GlobalPrincipalReactiveOAuth2AuthorizedClientService
import java.time.Duration
import kotlin.apply as kotlinApply

private const val DEFAULT_TIMEOUT_SECONDS: Long = 30
private const val DEFAULT_HEALTH_TIMEOUT_SECONDS: Long = 2

@AutoConfigureAfter(OAuth2ClientWebSecurityAutoConfiguration::class)
@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnBean(ClientRegistrationRepository::class)
@Configuration
class HmppsWebClientConfiguration {

  /**
   * This method generates an instance of the [AuthorizedClientServiceOAuth2AuthorizedClientManager]
   * class configured to cache all OAuth2 tokens under a single **principalName** using the
   * [GlobalPrincipalOAuth2AuthorizedClientService].
   *
   * The purpose of this [OAuth2AuthorizedClientManager] is to avoid unnecessary token requests to HMPPS Auth,
   * and it should be used for web clients where a username is being injected to the token request.
   *
   * @param clientRegistrationRepository
   * @param oAuth2AuthorizedClientProvider
   */
  @ConditionalOnMissingBean
  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientProvider: OAuth2AuthorizedClientProvider,
  ): OAuth2AuthorizedClientManager {
    val globalPrincipalOAuth2AuthorizedClientService =
      GlobalPrincipalOAuth2AuthorizedClientService(clientRegistrationRepository)
    return AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      globalPrincipalOAuth2AuthorizedClientService,
    ).kotlinApply {
      setAuthorizedClientProvider(oAuth2AuthorizedClientProvider)
    }
  }

  @ConditionalOnMissingBean
  @Bean
  fun authorizedClientProvider(): OAuth2AuthorizedClientProvider = oAuth2AuthorizedClientProvider(
    Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS),
  )
}

@AutoConfigureAfter(ReactiveOAuth2ClientWebSecurityAutoConfiguration::class)
@ConditionalOnWebApplication(type = REACTIVE)
@ConditionalOnBean(ReactiveClientRegistrationRepository::class)
@Configuration
class HmppsReactiveWebClientConfiguration {

  /**
   * This method generates an instance of the [AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager]
   * class configured to cache all OAuth2 tokens under a single **principalName** using the
   * [GlobalPrincipalReactiveOAuth2AuthorizedClientService].
   *
   * The purpose of this [ReactiveOAuth2AuthorizedClientManager] is to avoid unnecessary token requests to HMPPS Auth,
   * and it should be used for web clients where a username is being injected to the token request.
   *
   * @param reactiveClientRegistrationRepository
   * @param reactiveOAuth2AuthorizedClientProvider
   */
  @ConditionalOnMissingBean
  @Bean
  fun reactiveOAuth2AuthorizedClientManager(
    reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
    reactiveOAuth2AuthorizedClientProvider: ReactiveOAuth2AuthorizedClientProvider,
  ): ReactiveOAuth2AuthorizedClientManager = AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
    reactiveClientRegistrationRepository,
    GlobalPrincipalReactiveOAuth2AuthorizedClientService(reactiveClientRegistrationRepository),
  ).kotlinApply {
    setAuthorizedClientProvider(reactiveOAuth2AuthorizedClientProvider)
  }

  @ConditionalOnMissingBean
  @Bean
  fun reactiveOAuth2AuthorizedClientProvider(
    builder: WebClient.Builder,
  ): ReactiveOAuth2AuthorizedClientProvider = builder.reactiveOAuth2AuthorizedClientProvider(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
}

fun oAuth2AuthorizedClientProvider(clientCredentialsClientTimeout: Duration): OAuth2AuthorizedClientProvider {
  // TODO We have to use a deprecated class here because that's what Spring Security uses. If Spring Security switches to RestClientClientCredentialsTokenResponseClient then we can do the same.
  val accessTokenResponseClient = DefaultClientCredentialsTokenResponseClient().kotlinApply {
    setRestOperations(
      RestTemplate(listOf(FormHttpMessageConverter(), OAuth2AccessTokenResponseHttpMessageConverter())).kotlinApply {
        errorHandler = OAuth2ErrorResponseErrorHandler()
        requestFactory = SimpleClientHttpRequestFactory().kotlinApply {
          setReadTimeout(clientCredentialsClientTimeout)
        }
      },
    )
  }
  return OAuth2AuthorizedClientProviderBuilder
    .builder()
    .clientCredentials { it.accessTokenResponseClient(accessTokenResponseClient).build() }
    .build()
}

fun WebClient.Builder.reactiveOAuth2AuthorizedClientProvider(clientCredentialsClientTimeout: Duration): ReactiveOAuth2AuthorizedClientProvider {
  val accessTokenResponseClient = WebClientReactiveClientCredentialsTokenResponseClient().kotlinApply {
    setWebClient(
      this@reactiveOAuth2AuthorizedClientProvider.clientConnector(
        ReactorClientHttpConnector(
          HttpClient.create().responseTimeout(clientCredentialsClientTimeout),
        ),
      ).build(),
    )
  }
  return ReactiveOAuth2AuthorizedClientProviderBuilder
    .builder()
    .clientCredentials { it.accessTokenResponseClient(accessTokenResponseClient).build() }
    .build()
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
): WebClient = baseUrl(url)
  .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(healthTimeout)))
  .build()

fun WebClient.Builder.reactiveAuthorisedWebClient(
  authorizedClientManager: ReactiveOAuth2AuthorizedClientManager,
  registrationId: String,
  url: String,
  timeout: Duration = Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS),
): WebClient = baseUrl(url)
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
): WebClient = baseUrl(url)
  .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(healthTimeout)))
  .build()
