package uk.gov.justice.hmpps.kotlin.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.net.URI

@ExtendWith(MockitoExtension::class)
class HmppsWebClientConfigurationTest {

  @Nested
  inner class ExchangeFilterFunctionTests {
    @Mock
    private lateinit var mockAuthentication: Authentication

    @Mock
    private lateinit var mockSecurityContext: SecurityContext

    private lateinit var mockReactiveSecurityContextHolder: MockedStatic<ReactiveSecurityContextHolder>

    @BeforeEach()
    fun setup() {
      mockReactiveSecurityContextHolder = mockStatic(ReactiveSecurityContextHolder::class.java)
      mockReactiveSecurityContextHolder.`when`<Mono<SecurityContext>> { ReactiveSecurityContextHolder.getContext() }
        .thenReturn(Mono.just(mockSecurityContext))
    }

    @AfterEach
    fun teardown() {
      mockReactiveSecurityContextHolder.close()
    }

    @Test
    fun `usernameInjectingReactiveExchangeFilterFunction should inject username into form body`() {
      val testUser = "test-user"
      whenever(mockSecurityContext.authentication).thenReturn(mockAuthentication)
      whenever(mockAuthentication.name).thenReturn(testUser)

      val body = runFilterOnRequestAndReturnCapturedBody(usernameInjectingReactiveExchangeFilterFunction())

      assertThat(body is BodyInserters.FormInserter<*>)
      assertThat(body.toString().contains("grant_type=client_credentials"))
      assertThat(body.toString().contains("username=$testUser"))
    }

    @Test
    fun `usernameInjectingReactiveExchangeFilterFunction should not inject an empty username into the form body`() {
      whenever(mockSecurityContext.authentication).thenReturn(mockAuthentication)
      whenever(mockAuthentication.name).thenReturn("")

      val body = runFilterOnRequestAndReturnCapturedBody(usernameInjectingReactiveExchangeFilterFunction())

      assertThat(body is BodyInserters.FormInserter<*>)
      assertThat(body.toString().contains("grant_type=client_credentials"))
      assertThat(!body.toString().contains("username"))
    }

    @Test
    fun `usernameInjectingReactiveExchangeFilterFunction should handle a null authentication object`() {
      whenever(mockSecurityContext.authentication).thenReturn(null)

      val body = runFilterOnRequestAndReturnCapturedBody(usernameInjectingReactiveExchangeFilterFunction())

      assertThat(body is BodyInserters.FormInserter<*>)
      assertThat(body.toString().contains("grant_type=client_credentials"))
      assertThat(!body.toString().contains("username"))
    }

    private fun runFilterOnRequestAndReturnCapturedBody(filterToTest: ExchangeFilterFunction): BodyInserter<*, in ClientHttpRequest>? {
      val dummyClientRequest = ClientRequest
        .create(HttpMethod.POST, URI.create("auth/oauth/token"))
        .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
        .build()

      var capturedRequest: ClientRequest? = null

      val exchangeFunction = ExchangeFunction { req ->
        capturedRequest = req
        Mono.just(ClientResponse.create(HttpStatus.OK).build())
      }

      filterToTest.filter(dummyClientRequest, exchangeFunction).block()

      return capturedRequest?.body()
    }
  }
}
