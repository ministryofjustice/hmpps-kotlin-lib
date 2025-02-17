package uk.gov.justice.digital.hmpps.testappreactive.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareAuthenticationToken
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareReactiveTokenConverter
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.kotlin.auth.HmppsReactiveAuthenticationHolder
import uk.gov.justice.hmpps.kotlin.auth.extractAuthorities

@RestController
@PreAuthorize("hasRole('ROLE_CUSTOM_CONVERTER')")
class CustomConverterResource(private val authenticationHolder: HmppsReactiveAuthenticationHolder) {

  @GetMapping("/active-caseload")
  suspend fun getActiveCaseload() = CaseloadDetails((authenticationHolder.getAuthentication() as CustomAuthAwareAuthenticationToken).activeCaseload)

  data class CaseloadDetails(val activeCaseload: String)
}

class CustomAuthAwareAuthenticationToken(
  jwt: Jwt,
  clientId: String,
  userName: String? = null,
  authSource: AuthSource = AuthSource.NONE,
  authorities: Collection<GrantedAuthority> = emptyList(),
  val activeCaseload: String,
) : AuthAwareAuthenticationToken(jwt, clientId, userName, authSource, authorities)

class CustomAuthAwareAuthenticationTokenConverter(private val activeCaseloadProvider: () -> String) : AuthAwareReactiveTokenConverter() {
  override fun convert(jwt: Jwt): Mono<AuthAwareAuthenticationToken> = super.convert(jwt)
    .map { authAwareAuthenticationToken ->
      CustomAuthAwareAuthenticationToken(
        jwt,
        authAwareAuthenticationToken.clientId,
        authAwareAuthenticationToken.userName,
        authAwareAuthenticationToken.authSource,
        extractAuthorities(jwt, JwtGrantedAuthoritiesConverter()),
        activeCaseloadProvider.invoke(),
      )
    }
}
