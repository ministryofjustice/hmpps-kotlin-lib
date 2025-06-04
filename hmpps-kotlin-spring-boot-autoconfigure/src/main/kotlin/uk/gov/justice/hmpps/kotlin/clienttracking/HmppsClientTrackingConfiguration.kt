package uk.gov.justice.hmpps.kotlin.clienttracking

import com.nimbusds.jwt.SignedJWT
import io.opentelemetry.api.trace.Span
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.text.ParseException

internal typealias SetTrackingDetails = Any.(String) -> Unit

@Configuration
@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnMissingBean(name = ["clientTrackingInterceptor"])
@Import(HmppsClientTrackingInterceptorConfiguration::class)
class HmppsClientTrackingConfiguration(private val clientTrackingInterceptor: HmppsClientTrackingInterceptor) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    log.info("Adding application insights client tracking interceptor from hmpps-spring-boot-kotlin")
    registry.addInterceptor(clientTrackingInterceptor)
      .addPathPatterns(clientTrackingInterceptor.includePaths)
      .excludePathPatterns(clientTrackingInterceptor.excludePaths)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

@Configuration
@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnMissingBean(name = ["clientTrackingInterceptor"])
class HmppsClientTrackingInterceptorConfiguration {
  @Bean
  @ConditionalOnMissingBean
  fun hmppsClientTrackingInterceptor() = HmppsClientTrackingInterceptor()
}

@Configuration
@ConditionalOnWebApplication(type = REACTIVE)
@ConditionalOnMissingBean(name = ["clientTrackingWebFilter"])
class HmppsReactiveClientTrackingConfiguration {
  @Bean
  @ConditionalOnMissingFilterBean
  fun hmppsClientTrackingWebFilter() = HmppsClientTrackingWebFilter().also { log.info("Adding application insights client tracking web filter from hmpps-spring-boot-kotlin") }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

internal fun Any.defaultTrackingDetails(token: String) {
  val log = LoggerFactory.getLogger(this::class.java)
  if (token.startsWith("Bearer ") == true) {
    try {
      val currentSpan = Span.current()
      val jwtBody = SignedJWT.parse(token.replace("Bearer ", "")).jwtClaimsSet
      val user = jwtBody.getClaim("user_name")?.toString()
      user?.run {
        currentSpan.setAttribute("username", this) // username in customDimensions
        currentSpan.setAttribute("enduser.id", this) // user_Id at the top level of the request
      }
      val clientId = jwtBody.getClaim("client_id")?.toString()
      clientId?.run { currentSpan.setAttribute("clientId", this) }
        ?: { log?.warn("Unable to find clientId in token") }
    } catch (e: ParseException) {
      log?.warn("problem decoding jwt public key for application insights", e)
    }
  }
}
