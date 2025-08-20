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
import org.springframework.http.client.ReactorClientHttpRequestFactory
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.hmpps.kotlin.auth.service.GlobalPrincipalOAuth2AuthorizedClientService
import uk.gov.justice.hmpps.kotlin.auth.service.ReactiveGlobalPrincipalOAuth2AuthorizedClientService
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
   * The purpose of this [OAuth2AuthorizedClientManager] is to avoid unnecessary token requests to HMPPS Auth.
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
   * [ReactiveGlobalPrincipalOAuth2AuthorizedClientService].
   *
   * The purpose of this [ReactiveOAuth2AuthorizedClientManager] is to avoid unnecessary token requests to HMPPS Auth.
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
    ReactiveGlobalPrincipalOAuth2AuthorizedClientService(reactiveClientRegistrationRepository),
  ).kotlinApply {
    setAuthorizedClientProvider(reactiveOAuth2AuthorizedClientProvider)
  }

  @ConditionalOnMissingBean
  @Bean
  fun reactiveOAuth2AuthorizedClientProvider(
    builder: WebClient.Builder,
  ): ReactiveOAuth2AuthorizedClientProvider = builder.reactiveOAuth2AuthorizedClientProvider(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
}

fun oAuth2AuthorizedClientProvider(clientCredentialsClientTimeout: Duration): OAuth2AuthorizedClientProvider = OAuth2AuthorizedClientProviderBuilder
  .builder()
  .clientCredentials { it.accessTokenResponseClient(createAccessTokenResponseClient(clientCredentialsClientTimeout)) }
  .build()

private fun createAccessTokenResponseClient(clientCredentialsClientTimeout: Duration): RestClientClientCredentialsTokenResponseClient = RestClientClientCredentialsTokenResponseClient().kotlinApply {
  val requestFactory = ReactorClientHttpRequestFactory().kotlinApply {
    setReadTimeout(clientCredentialsClientTimeout)
  }

  // code duplicated from AbstractRestClientOAuth2AccessTokenResponseClient.restClient
  // so that we can set our requestFactory
  val restClient = RestClient.builder()
    .messageConverters { messageConverters: MutableList<HttpMessageConverter<*>?> ->
      messageConverters.clear()
      messageConverters.add(FormHttpMessageConverter())
      messageConverters.add(OAuth2AccessTokenResponseHttpMessageConverter())
    }
    .defaultStatusHandler(OAuth2ErrorResponseErrorHandler())
    .requestFactory(requestFactory)
    .build()

  setRestClient(restClient)
}

fun WebClient.Builder.reactiveOAuth2AuthorizedClientProvider(clientCredentialsClientTimeout: Duration): ReactiveOAuth2AuthorizedClientProvider {
  val accessTokenResponseClient = WebClientReactiveClientCredentialsTokenResponseClient().kotlinApply {
    setWebClient(
      clientConnector(
        ReactorClientHttpConnector(
          HttpClient.create().responseTimeout(clientCredentialsClientTimeout),
        ),
      ).build(),
    )
  }
  return ReactiveOAuth2AuthorizedClientProviderBuilder
    .builder()
    .clientCredentials { it.accessTokenResponseClient(accessTokenResponseClient) }
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

/**
 * This method generates an instance of the [AuthorizedClientServiceOAuth2AuthorizedClientManager]
 * class configured to include the name of the authenticated principal in the OAuth2ClientCredentialsGrantRequest. This is designed to be used as part of
 * a request scoped web client where the authenticated principal can vary between requests.
 *
 * A [RestClientClientCredentialsTokenResponseClient] is configured to extract
 * the principal name from the current [Authentication] object and sets it as the **username** parameter
 * on the client credentials token request.
 *
 * This should be used for web clients where the user context is required.
 *
 * @param clientRegistrationRepository
 * @param OAuth2AuthorizedClientService
 */
fun usernameAwareTokenRequestOAuth2AuthorizedClientManager(
  clientRegistrationRepository: ClientRegistrationRepository,
  oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  clientCredentialsTokenRequestTimeout: Duration,
): OAuth2AuthorizedClientManager {
  val usernameAwareRestClientClientCredentialsTokenResponseClient =
    createAccessTokenResponseClient(clientCredentialsTokenRequestTimeout).kotlinApply {
      val username = SecurityContextHolder.getContext().authentication.name

      setParametersCustomizer { it.add("username", username) }
    }

  val oAuth2AuthorizedClientProvider =
    OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials {
      it.accessTokenResponseClient(usernameAwareRestClientClientCredentialsTokenResponseClient)
    }.build()

  return AuthorizedClientServiceOAuth2AuthorizedClientManager(
    clientRegistrationRepository,
    oAuth2AuthorizedClientService,
  ).kotlinApply {
    setAuthorizedClientProvider(oAuth2AuthorizedClientProvider)
  }
}

/**
 * This method generates an instance of the [AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager]
 * class configured to include the name of the authenticated principal in the OAuth2ClientCredentialsGrantRequest. This is designed to be used as part of
 * a request scoped web client where the authenticated principal can vary between requests.
 *
 * A [WebClientReactiveClientCredentialsTokenResponseClient] is configured with an exchange filter function to extract
 * the principal name from the current [org.springframework.security.core.Authentication] object in the [ReactiveSecurityContextHolder]
 * and set it as the **username** parameter on the client credentials token request.
 *
 * This should be used for web clients where the user context is required.
 *
 * @param reactiveClientRegistrationRepository
 * @param reactiveOAuth2AuthorizedClientService
 * @param clientCredentialsRequestTimeout
 */
fun reactiveUsernameAwareTokenRequestOAuth2AuthorizedClientManager(
  reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
  reactiveOAuth2AuthorizedClientService: ReactiveOAuth2AuthorizedClientService,
  clientCredentialsRequestTimeout: Duration,
): ReactiveOAuth2AuthorizedClientManager {
  val usernameAwareWebClientReactiveClientCredentialsTokenResponseClient =
    WebClientReactiveClientCredentialsTokenResponseClient().configureWebClient(clientCredentialsRequestTimeout, listOf(usernameInjectingReactiveExchangeFilterFunction()))

  val reactiveAuthorizedClientProvider =
    ReactiveOAuth2AuthorizedClientProviderBuilder.builder().clientCredentials { builder ->
      builder.accessTokenResponseClient(
        usernameAwareWebClientReactiveClientCredentialsTokenResponseClient,
      )
    }.build()

  return AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
    reactiveClientRegistrationRepository,
    reactiveOAuth2AuthorizedClientService,
  ).kotlinApply {
    setAuthorizedClientProvider(reactiveAuthorizedClientProvider)
  }
}

fun usernameInjectingReactiveExchangeFilterFunction(): ExchangeFilterFunction = ExchangeFilterFunction.ofRequestProcessor { request ->
  ReactiveSecurityContextHolder.getContext().map { securityContext ->
    val username = securityContext?.authentication?.name
    val builder = ClientRequest.from(request)
    val body = request.body()
    if (!username.isNullOrEmpty() && body is BodyInserters.FormInserter<*>) {
      @Suppress("UNCHECKED_CAST")
      builder.body((body as BodyInserters.FormInserter<String>).with("username", username))
    }
    builder.build()
  }
}

private fun WebClientReactiveClientCredentialsTokenResponseClient.configureWebClient(clientCredentialsRequestTimeout: Duration, filterFunctions: Collection<ExchangeFilterFunction> = emptyList<ExchangeFilterFunction>()): WebClientReactiveClientCredentialsTokenResponseClient = this.kotlinApply {
  setWebClient(
    WebClient.builder()
      .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(clientCredentialsRequestTimeout)))
      .filters { it.addAll(filterFunctions) }
      .build(),
  )
}
