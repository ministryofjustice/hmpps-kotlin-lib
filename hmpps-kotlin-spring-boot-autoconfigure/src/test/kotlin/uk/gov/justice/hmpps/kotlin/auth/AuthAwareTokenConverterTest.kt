package uk.gov.justice.hmpps.kotlin.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Duration
import java.time.Instant
import java.util.UUID

class AuthAwareTokenConverterTest {
  @Test
  fun `should take user_name as default principal`() {
    val jwt = createJwt(
      claims = mapOf(
        "user_name" to "some user name",
        "client_id" to "some client id",
      ),
    )

    val token = AuthAwareTokenConverter().convert(jwt)

    assertThat(token.principal).isEqualTo("some user name")
  }

  @Test
  fun `should fall back on client_id for principal`() {
    val jwt = createJwt(
      claims = mapOf(
        "client_id" to "some client id",
      ),
    )

    val token = AuthAwareTokenConverter().convert(jwt)

    assertThat(token.principal).isEqualTo("some client id")
  }

  @Test
  fun `should throw if no principal`() {
    val jwt = createJwt()

    assertThrows<NullPointerException> {
      AuthAwareTokenConverter().convert(jwt)
    }
  }

  @Test
  fun `should add granted authorities`() {
    val jwt = createJwt(
      claims = mapOf(
        "client_id" to "some client id",
        "authorities" to listOf("ROLE_SOME_ROLE", "SCOPE_SOME_SCOPE"),
      ),
    )

    val token = AuthAwareTokenConverter().convert(jwt)

    assertThat(token.authorities).containsExactlyInAnyOrder(
      SimpleGrantedAuthority("ROLE_SOME_ROLE"),
      SimpleGrantedAuthority("SCOPE_SOME_SCOPE"),
    )
  }

  @Test
  fun `should add auth source from claim`() {
    val jwt = createJwt(
      claims = mapOf(
        "client_id" to "some client id",
        "auth_source" to "nomis",
      ),
    )

    val token = AuthAwareTokenConverter().convert(jwt)

    assertThat(token.authSource).isEqualTo(AuthSource.NOMIS)
  }

  @Test
  fun `should fall back to none if no auth source`() {
    val jwt = createJwt(
      claims = mapOf(
        "client_id" to "some client id",
      ),
    )

    val token = AuthAwareTokenConverter().convert(jwt)

    assertThat(token.authSource).isEqualTo(AuthSource.NONE)
  }

  @Test
  fun `should be system client credentials`() {
    val jwt = createJwt(
      claims = mapOf(
        "client_id" to "some client id",
      ),
    )

    val token = AuthAwareTokenConverter().convert(jwt)

    assertThat(token.isSystemClientCredentials()).isTrue()
  }

  @Test
  fun `should not be system client credentials`() {
    val jwt = createJwt(
      claims = mapOf(
        "user_name" to "some user name",
        "client_id" to "some client id",
      ),
    )

    val token = AuthAwareTokenConverter().convert(jwt)

    assertThat(token.isSystemClientCredentials()).isFalse()
  }

  private fun createJwt(
    subject: String = "some_subject",
    claims: Map<String, Any> = mapOf(),
    expiryTime: Duration = Duration.ofHours(1),
    jwtId: String = UUID.randomUUID().toString(),
  ): Jwt = Jwt.withTokenValue("some token")
    .jti(jwtId)
    .subject(subject)
    .header("typ", "JWT")
    .apply {
      claims.forEach { (key, value) -> claim(key, value) }
    }
    .expiresAt(Instant.now().plus(expiryTime))
    .build()
}
