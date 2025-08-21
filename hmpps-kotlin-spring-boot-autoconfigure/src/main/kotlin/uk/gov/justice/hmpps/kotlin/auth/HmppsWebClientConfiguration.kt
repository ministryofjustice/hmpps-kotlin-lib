package uk.gov.justice.hmpps.kotlin.auth

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.hmpps.kotlin.auth.service.GlobalPrincipalOAuth2AuthorizedClientService
import uk.gov.justice.hmpps.kotlin.auth.service.ReactiveGlobalPrincipalOAuth2AuthorizedClientService
import java.time.Duration
import kotlin.apply as kotlinApply

private const val DEFAULT_TIMEOUT_SECONDS: Long = 30
private const val DEFAULT_HEALTH_TIMEOUT_SECONDS: Long = 2

@AutoConfigureBefore(OAuth2ClientAutoConfiguration::class)
@ConditionalOnWebApplication(type = SERVLET)
@EnableConfigurationProperties(OAuth2ClientProperties::class)
@Configuration
class HmppsWebClientConfiguration {

  @ConditionalOnMissingBean
  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
    oAuth2AuthorizedClientProvider: OAuth2AuthorizedClientProvider,
  ): OAuth2AuthorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
    clientRegistrationRepository,
    oAuth2AuthorizedClientService,
  ).kotlinApply {
    setAuthorizedClientProvider(oAuth2AuthorizedClientProvider)
  }

  /**
   * This method generates an instance of the [GlobalPrincipalOAuth2AuthorizedClientService]
   * class configured to cache all OAuth2 tokens under a single **principalName**.
   *
   * The purpose of this [OAuth2AuthorizedClientService] is to avoid unnecessary token requests to HMPPS Auth.
   * This should be the default [OAuth2AuthorizedClientService] unless the [WebClient] using it is configured to
   * inject the name of the authenticated principal into the client credentials token request. In this scenario the tokens
   * will be unique per user so a [org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService]
   * should be used instead.
   *
   * @param clientRegistrationRepository
   */
  @ConditionalOnMissingBean
  @Bean
  fun globalPrincipalOAuth2AuthorizedClientService(clientRegistrationRepository: ClientRegistrationRepository): OAuth2AuthorizedClientService = GlobalPrincipalOAuth2AuthorizedClientService(clientRegistrationRepository)

  @ConditionalOnMissingBean
  @Bean
  fun authorizedClientProvider(): OAuth2AuthorizedClientProvider = oAuth2AuthorizedClientProvider(
    Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS),
  )

  @ConditionalOnMissingBean
  @Bean
  fun clientRegistrationRepository(properties: OAuth2ClientProperties?): InMemoryClientRegistrationRepository {
    val registrations: MutableList<ClientRegistration?> = ArrayList<ClientRegistration?>(
      OAuth2ClientPropertiesMapper(properties).asClientRegistrations().values,
    )
    return InMemoryClientRegistrationRepository(registrations)
  }
}

@AutoConfigureBefore(ReactiveOAuth2ClientAutoConfiguration::class)
@ConditionalOnWebApplication(type = REACTIVE)
@EnableConfigurationProperties(OAuth2ClientProperties::class)
@Configuration
class HmppsReactiveWebClientConfiguration {

  @ConditionalOnMissingBean
  @Bean
  fun reactiveOAuth2AuthorizedClientManager(
    reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
    reactiveOAuth2AuthorizedClientService: ReactiveOAuth2AuthorizedClientService,
    reactiveOAuth2AuthorizedClientProvider: ReactiveOAuth2AuthorizedClientProvider,
  ): ReactiveOAuth2AuthorizedClientManager = AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
    reactiveClientRegistrationRepository,
    reactiveOAuth2AuthorizedClientService,
  ).kotlinApply {
    setAuthorizedClientProvider(reactiveOAuth2AuthorizedClientProvider)
  }

  /**
   * This method generates an instance of the [ReactiveGlobalPrincipalOAuth2AuthorizedClientService]
   * class configured to cache all OAuth2 tokens under a single **principalName**.
   *
   * The purpose of this [ReactiveOAuth2AuthorizedClientService] is to avoid unnecessary token requests to HMPPS Auth.
   * This should be the default [ReactiveOAuth2AuthorizedClientService] unless the [WebClient] using it is configured to
   * inject the name of the authenticated principal into the client credentials token request. In this scenario the tokens
   * will be unique per user so a [org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService]
   * should be used instead.
   *
   * @param reactiveClientRegistrationRepository
   */
  @ConditionalOnMissingBean
  @Bean
  fun reactiveGlobalPrincipalOAuth2AuthorizedClientService(reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository): ReactiveOAuth2AuthorizedClientService = ReactiveGlobalPrincipalOAuth2AuthorizedClientService(reactiveClientRegistrationRepository)

  @ConditionalOnMissingBean
  @Bean
  fun reactiveClientRegistrationRepository(properties: OAuth2ClientProperties): InMemoryReactiveClientRegistrationRepository {
    val registrations: MutableList<ClientRegistration?> = java.util.ArrayList<ClientRegistration?>(
      OAuth2ClientPropertiesMapper(properties).asClientRegistrations().values,
    )
    return InMemoryReactiveClientRegistrationRepository(registrations)
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
