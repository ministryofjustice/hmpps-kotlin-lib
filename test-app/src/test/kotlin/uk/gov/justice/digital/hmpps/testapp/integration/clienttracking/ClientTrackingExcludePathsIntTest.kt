package uk.gov.justice.digital.hmpps.testapp.integration.clienttracking

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.testapp.integration.IntegrationTestBase
import uk.gov.justice.hmpps.kotlin.clienttracking.HmppsClientTrackingInterceptor

/*
 * This class tests that the tracking interceptors exclude paths are respected
 */
@Import(ClientTrackingExcludePathsIntTest.CustomConfiguration::class)
class ClientTrackingExcludePathsIntTest(
  @MockitoSpyBean @Qualifier("customClientTrackingInterceptor") private val interceptorSpy: HmppsClientTrackingInterceptor,
) : IntegrationTestBase() {

  @TestConfiguration
  class CustomConfiguration {
    @Bean
    fun customClientTrackingInterceptor() = HmppsClientTrackingInterceptor(excludePaths = listOf("/time"))
  }

  @Test
  fun `should call interceptor`() {
    webTestClient.get()
      .uri("/prisoner/A1111AA/booking")
      .header(HttpHeaders.AUTHORIZATION, "Bearer ${jwtAuthHelper.createJwtAccessToken(clientId = "some-client", roles = listOf("ROLE_TEST_APP"))}")
      .exchange()
      .expectStatus().isOk

    verify(interceptorSpy).preHandle(any(), any(), any())
  }

  @Test
  fun `should not call interceptor for excluded paths`() {
    webTestClient.get()
      .uri("/time")
      .header(HttpHeaders.AUTHORIZATION, "Bearer ${jwtAuthHelper.createJwtAccessToken(clientId = "some-client", roles = listOf("ROLE_TEST_APP"))}")
      .exchange()
      .expectStatus().isOk

    verify(interceptorSpy, never()).preHandle(any(), any(), any())
  }
}
