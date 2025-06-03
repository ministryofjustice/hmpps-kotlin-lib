package uk.gov.justice.digital.hmpps.testappreactive.integration.clienttracking

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.testappreactive.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.testappreactive.integration.clienttracking.ClientTrackingUnobtrusiveTest.CustomConfiguration.ClientTrackingWebFilter
import uk.gov.justice.hmpps.kotlin.clienttracking.HmppsClientTrackingWebFilter

/*
 * This tests that if an application already has a bean called "clientTrackingWebFilter" then the library will not do anything.
 */
@Import(ClientTrackingUnobtrusiveTest.CustomConfiguration::class)
class ClientTrackingUnobtrusiveTest(
  @Autowired private val hmppsLibraryWebFilter: HmppsClientTrackingWebFilter? = null,
  @MockitoSpyBean @Qualifier("clientTrackingWebFilter") private val existingWebFilterSpy: ClientTrackingWebFilter,
) : IntegrationTestBase() {

  @TestConfiguration
  class CustomConfiguration {
    @Bean
    fun clientTrackingWebFilter() = ClientTrackingWebFilter()

    class ClientTrackingWebFilter : WebFilter {
      override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> = chain.filter(exchange)
    }
  }

  @Test
  fun `should call existing filter and not create the HMPPS filter`() {
    assertThat(hmppsLibraryWebFilter).isNull()

    webTestClient.get()
      .uri("/prisoner/A1111AA/booking")
      .header(HttpHeaders.AUTHORIZATION, "Bearer ${jwtAuthHelper.createJwtAccessToken(clientId = "some-client", roles = listOf("ROLE_TEST_APP_REACTIVE"))}")
      .exchange()
      .expectStatus().isOk

    verify(existingWebFilterSpy).filter(any(), any())
  }
}
