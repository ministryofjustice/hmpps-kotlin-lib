package uk.gov.justice.digital.hmpps.testappreactive.integration.clienttracking

import io.jsonwebtoken.Jwts
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
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
import java.security.KeyPairGenerator
import java.time.Duration
import java.util.Date
import java.util.UUID
import kotlin.run
import kotlin.use

/*
 * This class tests the behaviour of the default setTrackingDetails implementation
 */
@Import(JwtAuthorisationHelper::class, HmppsReactiveClientTrackingConfiguration::class, HmppsClientTrackingWebFilter::class)
@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class])
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
class ClientTrackingDetailsDefaultsTest(
  @Autowired private val hmppsClientTrackingWebFilter: HmppsClientTrackingWebFilter,
  @Autowired private val jwtAuthHelper: JwtAuthorisationHelper,
) {

  private val tracer: Tracer = otelTesting.openTelemetry.getTracer("test")

  private val webFilterChain: WebFilterChain = mock()

  @BeforeEach
  internal fun setup() {
    whenever(webFilterChain.filter(any())).thenReturn(Mono.empty())
  }

  @Test
  fun `should add clientId and username to telemetry`() {
    val token = jwtAuthHelper.createJwtAccessToken(username = "bob", clientId = "some-client")
    val req = MockServerHttpRequest.get("/time").header(HttpHeaders.AUTHORIZATION, "Bearer $token").build()
    val exchange = MockServerWebExchange.from(req)

    tracer.spanBuilder("span").startSpan().run {
      makeCurrent().use { hmppsClientTrackingWebFilter.filter(exchange, webFilterChain).block() }
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
    val req = MockServerHttpRequest.get("/time").header(HttpHeaders.AUTHORIZATION, "Bearer $token").build()
    val exchange = MockServerWebExchange.from(req)

    tracer.spanBuilder("span").startSpan().run {
      makeCurrent().use { hmppsClientTrackingWebFilter.filter(exchange, webFilterChain).block() }
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
    val req = MockServerHttpRequest.get("/time").header(HttpHeaders.AUTHORIZATION, "Bearer $token").build()
    val exchange = MockServerWebExchange.from(req)

    assertDoesNotThrow {
      tracer.spanBuilder("span").startSpan().run {
        makeCurrent().use { hmppsClientTrackingWebFilter.filter(exchange, webFilterChain).block() }
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
