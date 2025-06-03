package uk.gov.justice.digital.hmpps.testapp.integration.clienttracking

import com.nimbusds.jwt.SignedJWT
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.hmpps.kotlin.clienttracking.HmppsClientTrackingConfiguration
import uk.gov.justice.hmpps.kotlin.clienttracking.HmppsClientTrackingInterceptor
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import kotlin.also
import kotlin.run
import kotlin.use

/*
 * This class tests that setTrackingDetails can be overridden and demonstrates how to populate client tracking data from
 * other beans.
 */
@Import(JwtAuthorisationHelper::class, ClientTrackingDetailsCustomTest.CustomConfiguration::class, HmppsClientTrackingConfiguration::class)
@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class])
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
class ClientTrackingDetailsCustomTest(
  @Autowired private val clientTrackingInterceptor: HmppsClientTrackingInterceptor,
  @Autowired private val jwtAuthHelper: JwtAuthorisationHelper,
) {
  private val tracer: Tracer = otelTesting.openTelemetry.getTracer("test")

  @TestConfiguration
  class CustomConfiguration {
    @Bean
    fun someBean() = "some-key"

    @Bean
    fun customClientTrackingInterceptor(someBean: String) = HmppsClientTrackingInterceptor { token ->
      val currentSpan = Span.current()
      val jwtBody = SignedJWT.parse(token.replace("Bearer ", "")).jwtClaimsSet
      currentSpan.setAttribute("my-client-id-key", jwtBody.getClaim("client_id").toString())
      currentSpan.setAttribute("some-bean-key", someBean)
    }
  }

  @Test
  fun `should use custom setTrackingDetails function`() {
    val token = jwtAuthHelper.createJwtAccessToken(clientId = "some-client")
    val req = MockHttpServletRequest().also { it.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token") }
    val res = MockHttpServletResponse()

    tracer.spanBuilder("span").startSpan().run {
      makeCurrent().use { clientTrackingInterceptor.preHandle(req, res, "null") }
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
