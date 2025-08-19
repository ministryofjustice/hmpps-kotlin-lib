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
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
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
   * and it should be used for web clients where the [usernameAwareTokenRequestOAuth2AuthorizedClientManager] is not in use.
   *
   * @param clientRegistrationRepository
   */
  @ConditionalOnMissingBean
  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
  ): OAuth2AuthorizedClientManager {
    val authorizedClientProvider =
      OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val globalPrincipalOAuth2AuthorizedClientService =
      GlobalPrincipalOAuth2AuthorizedClientService(clientRegistrationRepository)
    return AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      globalPrincipalOAuth2AuthorizedClientService,
    ).kotlinApply {
      setAuthorizedClientProvider(authorizedClientProvider)
    }
  }
}

@AutoConfigureAfter(ReactiveOAuth2ClientWebSecurityAutoConfiguration::class)
@ConditionalOnWebApplication(type = REACTIVE)
@ConditionalOnBean(ReactiveClientRegistrationRepository::class)
@Configuration
class HmppsReactiveWebClientConfiguration {

  @ConditionalOnMissingBean
  @Bean
  fun reactiveOAuth2AuthorizedClientManager(
    reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
  ): ReactiveOAuth2AuthorizedClientManager = AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
    reactiveClientRegistrationRepository,
    GlobalPrincipalReactiveOAuth2AuthorizedClientService(reactiveClientRegistrationRepository),
  ).kotlinApply {
    setAuthorizedClientProvider(
      ReactiveOAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build(),
    )
  }
}

fun WebClient.Builder.authorisedWebClient(
  authorizedClientManager: OAuth2AuthorizedClientManager,
  registrationId: String,
  url: String,
  timeout: Duration = Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS),
): WebClient {
  val oauth2Client =
    ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager).kotlinApply {
      setDefaultClientRegistrationId(registrationId)
    }

  return baseUrl(url).clientConnector(
    ReactorClientHttpConnector(
      HttpClient.create().responseTimeout(timeout),
    ),
  ).filter(oauth2Client).build()
}

fun WebClient.Builder.healthWebClient(
  url: String,
  healthTimeout: Duration = Duration.ofSeconds(DEFAULT_HEALTH_TIMEOUT_SECONDS),
): WebClient = baseUrl(url).clientConnector(
  ReactorClientHttpConnector(
    HttpClient.create().responseTimeout(healthTimeout),
  ),
).build()

fun WebClient.Builder.reactiveAuthorisedWebClient(
  authorizedClientManager: ReactiveOAuth2AuthorizedClientManager,
  registrationId: String,
  url: String,
  timeout: Duration = Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS),
): WebClient = baseUrl(url).clientConnector(
  ReactorClientHttpConnector(
    HttpClient.create().responseTimeout(timeout),
  ),
).filter(
  ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager).kotlinApply {
    setDefaultClientRegistrationId(registrationId)
  },
).build()

fun WebClient.Builder.reactiveHealthWebClient(
  url: String,
  healthTimeout: Duration = Duration.ofSeconds(DEFAULT_HEALTH_TIMEOUT_SECONDS),
): WebClient = baseUrl(url).clientConnector(
  ReactorClientHttpConnector(
    HttpClient.create().responseTimeout(healthTimeout),
  ),
).build()

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
): OAuth2AuthorizedClientManager {
  val usernameAwareRestClientClientCredentialsTokenResponseClient =
    RestClientClientCredentialsTokenResponseClient().kotlinApply {
      val username = SecurityContextHolder.getContext().authentication.name

      setParametersCustomizer { params ->
        params.add("username", username)
      }
    }

  val authorizedClientProvider =
    OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials { builder ->
      builder.accessTokenResponseClient(
        usernameAwareRestClientClientCredentialsTokenResponseClient,
      )
    }.build()

  return AuthorizedClientServiceOAuth2AuthorizedClientManager(
    clientRegistrationRepository,
    oAuth2AuthorizedClientService,
  ).kotlinApply {
    setAuthorizedClientProvider(authorizedClientProvider)
  }
}

fun usernameAwareReactiveTokenRequestOAuth2AuthorizedClientManager(
  reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
  reactiveOAuth2AuthorizedClientService: ReactiveOAuth2AuthorizedClientService,
): ReactiveOAuth2AuthorizedClientManager {
  val usernameAwareWebClientReactiveClientCredentialsTokenResponseClient =
    WebClientReactiveClientCredentialsTokenResponseClient().kotlinApply {
      setWebClient(WebClient.builder().filter(usernameInjectingReactiveExchangeFilterFunction()).build())
    }

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
  ReactiveSecurityContextHolder.getContext()
    .map(SecurityContext::getAuthentication)
    .map(Authentication::getName)
    .map { username ->
      val builder = ClientRequest.from(request)
      val body = request.body()
      if (body is BodyInserters.FormInserter<*>) {
        @Suppress("UNCHECKED_CAST")
        builder.body((body as BodyInserters.FormInserter<String>).with("username", username))
      }
      builder.build()
    }
}
