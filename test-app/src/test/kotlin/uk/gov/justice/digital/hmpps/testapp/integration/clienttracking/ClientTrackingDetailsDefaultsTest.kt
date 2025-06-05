package uk.gov.justice.digital.hmpps.testapp.integration.clienttracking

import io.jsonwebtoken.Jwts
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
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
import java.security.KeyPairGenerator
import java.time.Duration
import java.util.Date
import java.util.UUID
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

  @Test
  fun `should handle missing client id`() {
    val token = authTokenNoClientId()
    val req = MockHttpServletRequest().also { it.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token") }
    val res = MockHttpServletResponse()

    assertDoesNotThrow {
      tracer.spanBuilder("span").startSpan().run {
        makeCurrent().use { clientTrackingInterceptor.preHandle(req, res, "null") }
        end()
      }
    }
  }

  private fun authTokenNoClientId() = KeyPairGenerator
    .getInstance("RSA")
    .apply { initialize(2048) }
    .generateKeyPair()
    .let {
      Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject("subject")
        .expiration(Date(System.currentTimeMillis() + Duration.ofHours(2).toMillis()))
        .signWith(it.private, Jwts.SIG.RS256)
        .compact()
    }

  private companion object {
    @JvmStatic
    @RegisterExtension
    val otelTesting: OpenTelemetryExtension = OpenTelemetryExtension.create()
  }
}
