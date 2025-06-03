package uk.gov.justice.digital.hmpps.testapp.integration.clienttracking

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
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
 * This class tests the behaviour of the default setTrackingDetails implementation
 */
@Import(JwtAuthorisationHelper::class, HmppsClientTrackingInterceptor::class, HmppsClientTrackingConfiguration::class)
@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class])
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
class ClientTrackingDetailsDefaultsTest(
  @Autowired private val clientTrackingInterceptor: HmppsClientTrackingInterceptor,
  @Autowired private val jwtAuthHelper: JwtAuthorisationHelper,
) {
  private val tracer: Tracer = otelTesting.openTelemetry.getTracer("test")

  @Test
  fun `should add clientId and username to telemetry`() {
    val token = jwtAuthHelper.createJwtAccessToken(username = "bob", clientId = "some-client")
    val req = MockHttpServletRequest().also { it.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token") }
    val res = MockHttpServletResponse()

    tracer.spanBuilder("span").startSpan().run {
      makeCurrent().use { clientTrackingInterceptor.preHandle(req, res, "null") }
      end()
    }

    otelTesting.assertTraces().hasTracesSatisfyingExactly({ t ->
      t.hasSpansSatisfyingExactly({
        it.hasAttribute(AttributeKey.stringKey("username"), "bob")
        it.hasAttribute(AttributeKey.stringKey("enduser.id"), "bob")
        it.hasAttribute(AttributeKey.stringKey("clientId"), "some-client")
      })
    })
  }

  @Test
  fun `should only add clientId to telemetry if username is null`() {
    val token = jwtAuthHelper.createJwtAccessToken(clientId = "some-client")
    val req = MockHttpServletRequest().also { it.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token") }
    val res = MockHttpServletResponse()

    tracer.spanBuilder("span").startSpan().run {
      makeCurrent().use { clientTrackingInterceptor.preHandle(req, res, "null") }
      end()
    }

    otelTesting.assertTraces().hasTracesSatisfyingExactly({ t ->
      t.hasSpansSatisfyingExactly({
        it.hasAttribute(AttributeKey.stringKey("clientId"), "some-client")
      })
    })
  }

  private companion object {
    @JvmStatic
    @RegisterExtension
    val otelTesting: OpenTelemetryExtension = OpenTelemetryExtension.create()
  }
}
