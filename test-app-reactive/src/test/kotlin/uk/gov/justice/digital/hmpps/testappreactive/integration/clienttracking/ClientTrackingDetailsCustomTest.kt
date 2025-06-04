package uk.gov.justice.digital.hmpps.testappreactive.integration.clienttracking

import com.nimbusds.jwt.SignedJWT
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import uk.gov.justice.hmpps.kotlin.clienttracking.HmppsClientTrackingWebFilter
import uk.gov.justice.hmpps.kotlin.clienttracking.HmppsReactiveClientTrackingConfiguration
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import kotlin.run
import kotlin.text.replace
import kotlin.use

/*
 * This class tests that setTrackingDetails can be overridden and demonstrates how to populate client tracking data from
 * other beans.
 */
@Import(JwtAuthorisationHelper::class, HmppsReactiveClientTrackingConfiguration::class, ClientTrackingDetailsCustomTest.CustomConfiguration::class)
@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class])
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
class ClientTrackingDetailsCustomTest(
  @Autowired private val hmppsClientTrackingWebFilter: HmppsClientTrackingWebFilter,
  @Autowired private val jwtAuthHelper: JwtAuthorisationHelper,
) {

  @TestConfiguration
  class CustomConfiguration {
    @Bean
    fun someBean() = "some-key"

    @Bean
    fun customClientTrackingInterceptor(someBean: String) = HmppsClientTrackingWebFilter { token ->
      val currentSpan = Span.current()
      val jwtBody = SignedJWT.parse(token.replace("Bearer ", "")).jwtClaimsSet
      currentSpan.setAttribute("my-client-id-key", jwtBody.getClaim("client_id").toString())
      currentSpan.setAttribute("some-bean-key", someBean)
    }
  }

  private val tracer: Tracer = otelTesting.openTelemetry.getTracer("test")

  private val webFilterChain: WebFilterChain = mock()

  @BeforeEach
  internal fun setup() {
    whenever(webFilterChain.filter(any())).thenReturn(Mono.empty())
  }

  @Test
  fun `should use custom setTrackingDetails function`() {
    val token = jwtAuthHelper.createJwtAccessToken(clientId = "some-client")
    val req = MockServerHttpRequest.get("/time").header(HttpHeaders.AUTHORIZATION, "Bearer $token").build()
    val exchange = MockServerWebExchange.from(req)

    tracer.spanBuilder("span").startSpan().run {
      makeCurrent().use { hmppsClientTrackingWebFilter.filter(exchange, webFilterChain).block() }
      end()
    }

    otelTesting.assertTraces().hasTracesSatisfyingExactly({ t ->
      t.hasSpansSatisfyingExactly({
        it.hasAttribute(AttributeKey.stringKey("my-client-id-key"), "some-client")
        it.hasAttribute(AttributeKey.stringKey("some-bean-key"), "some-key")
      })
    })
  }

  private companion object {
    @JvmStatic
    @RegisterExtension
    val otelTesting: OpenTelemetryExtension = OpenTelemetryExtension.create()
  }
}
