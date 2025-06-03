package uk.gov.justice.digital.hmpps.testapp.integration.clienttracking

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
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
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.justice.digital.hmpps.testapp.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.testapp.integration.clienttracking.ClientTrackingUnobtrusiveTest.CustomConfiguration.ClientTrackingInterceptor
import uk.gov.justice.hmpps.kotlin.clienttracking.HmppsClientTrackingInterceptor

/*
 * This tests that if an application already has a bean called "clientTrackingInterceptor" then the library will not do anything.
 */
@Import(ClientTrackingUnobtrusiveTest.CustomConfiguration::class)
class ClientTrackingUnobtrusiveTest(
  @Autowired private val hmppsLibraryInterceptor: HmppsClientTrackingInterceptor? = null,
  @MockitoSpyBean @Qualifier("clientTrackingInterceptor") private val existingInterceptorSpy: ClientTrackingInterceptor,
) : IntegrationTestBase() {

  @TestConfiguration
  class CustomConfiguration {
    @TestConfiguration
    class ClientTrackingConfiguration(private val clientTrackingInterceptor: ClientTrackingInterceptor) : WebMvcConfigurer {
      override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(clientTrackingInterceptor).addPathPatterns("/**")
      }
    }

    @Bean
    fun clientTrackingInterceptor() = ClientTrackingInterceptor()

    class ClientTrackingInterceptor : HandlerInterceptor {
      override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean = true
    }
  }

  @Test
  fun `should call existing interceptor and not create the HMPPS interceptor`() {
    assertThat(hmppsLibraryInterceptor).isNull()

    webTestClient.get()
      .uri("/prisoner/A1111AA/booking")
      .header(HttpHeaders.AUTHORIZATION, "Bearer ${jwtAuthHelper.createJwtAccessToken(clientId = "some-client", roles = listOf("ROLE_TEST_APP"))}")
      .exchange()
      .expectStatus().isOk

    verify(existingInterceptorSpy).preHandle(any(), any(), any())
  }
}
