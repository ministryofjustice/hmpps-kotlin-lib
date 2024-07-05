package uk.gov.justice.hmpps.test.kotlin.auth

import io.jsonwebtoken.Jwts
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.Date
import java.util.UUID

/**
 * Class to provide a JWT Decoder so that we don't need to go to HMPPS Auth to retrieve the jwts.
 * <p>
 * Also provides methods to set the bearer authorisation on HttpHeaders and create an access token for use in WebClients.
 */
@Component
@ConditionalOnProperty(prefix = "hmpps", name = ["test.jwt-helper-enabled"], matchIfMissing = true)
class JwtAuthorisationHelper {
  private val keyPair: KeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

  @Bean
  @Primary
  @ConditionalOnWebApplication(type = SERVLET)
  fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()

  @Bean
  @Primary
  @ConditionalOnWebApplication(type = REACTIVE)
  fun reactiveJwtDecoder(): ReactiveJwtDecoder = NimbusReactiveJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()

  fun setAuthorisationHeader(
    clientId: String = "test-client-id",
    username: String? = null,
    scope: List<String> = listOf(),
    roles: List<String> = listOf(),
  ): (HttpHeaders) -> Unit {
    val token = createJwtAccessToken(
      clientId = clientId,
      username = username,
      scope = scope,
      roles = roles,
    )
    return { it.setBearerAuth(token) }
  }

  @JvmOverloads
  fun createJwtAccessToken(
    clientId: String = "test-client-id",
    username: String? = null,
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expiryTime: Duration = Duration.ofHours(2),
    jwtId: String = UUID.randomUUID().toString(),
    authSource: String = "none",
    grantType: String = "client_credentials",
  ): String =
    mutableMapOf<String, Any>(
      "sub" to (username ?: clientId),
      "client_id" to clientId,
      "auth_source" to authSource,
      "grant_type" to grantType,
    ).apply {
      username?.let { this["user_name"] = username }
      scope?.let { this["scope"] = scope }
      roles?.let {
        // ensure that all roles have a ROLE_ prefix
        this["authorities"] = roles.map { "ROLE_${it.substringAfter("ROLE_")}" }
      }
    }
      .let {
        Jwts.builder()
          .id(jwtId)
          .subject(username ?: clientId)
          .claims(it.toMap())
          .expiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
          .signWith(keyPair.private, Jwts.SIG.RS256)
          .compact()
      }
}
