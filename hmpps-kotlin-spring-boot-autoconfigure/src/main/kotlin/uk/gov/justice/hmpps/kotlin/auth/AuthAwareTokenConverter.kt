package uk.gov.justice.hmpps.kotlin.auth

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import reactor.core.publisher.Mono

open class AuthAwareTokenConverter : Converter<Jwt, AbstractAuthenticationToken> {
  private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> =
    JwtGrantedAuthoritiesConverter()

  override fun convert(jwt: Jwt) = convert(jwt, jwtGrantedAuthoritiesConverter)
}

open class AuthAwareReactiveTokenConverter : Converter<Jwt, Mono<AuthAwareAuthenticationToken>> {
  private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> =
    JwtGrantedAuthoritiesConverter()

  override fun convert(jwt: Jwt): Mono<AuthAwareAuthenticationToken> = Mono.just(convert(jwt, jwtGrantedAuthoritiesConverter))
}

fun convert(jwt: Jwt, converter: Converter<Jwt, Collection<GrantedAuthority>>): AuthAwareAuthenticationToken {
  val claims = jwt.claims
  val authorities = extractAuthorities(jwt, converter)
  return AuthAwareAuthenticationToken(
    jwt = jwt,
    clientId = findClientId(claims),
    userName = findUserName(claims),
    authSource = findAuthSource(claims),
    authorities = authorities,
  )
}

private fun findUserName(claims: Map<String, Any?>): String? = if (claims.containsKey("user_name")) claims["user_name"] as String else null

private fun findClientId(claims: Map<String, Any?>) = claims["client_id"] as String

private fun findAuthSource(claims: Map<String, Any?>) = AuthSource.findBySource(claims["auth_source"] as String?)

fun extractAuthorities(jwt: Jwt, converter: Converter<Jwt, Collection<GrantedAuthority>>): Collection<GrantedAuthority> {
  val authorities = mutableListOf<GrantedAuthority>().apply { addAll(converter.convert(jwt)!!) }
  if (jwt.claims.containsKey("authorities")) {
    @Suppress("UNCHECKED_CAST")
    val claimAuthorities = (jwt.claims["authorities"] as Collection<String>).toList()
    authorities.addAll(claimAuthorities.map(::SimpleGrantedAuthority))
  }
  return authorities.toSet()
}

open class AuthAwareAuthenticationToken(
  val jwt: Jwt,
  val clientId: String,
  val userName: String? = null,
  val authSource: AuthSource = AuthSource.NONE,
  authorities: Collection<GrantedAuthority> = emptyList(),
) : JwtAuthenticationToken(jwt, authorities) {
  override fun getPrincipal(): String = userName ?: clientId
}

enum class AuthSource(val source: String) {
  NONE("none"),
  NOMIS("nomis"),
  DELIUS("delius"),
  AUTH("auth"),
  ;

  companion object {
    fun findBySource(source: String?): AuthSource = entries.firstOrNull { it.source == source } ?: NONE
  }
}
