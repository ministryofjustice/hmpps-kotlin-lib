package uk.gov.justice.digital.hmpps.testapp.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareAuthenticationToken
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareTokenConverter
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.kotlin.auth.extractAuthorities

@RestController
@PreAuthorize("hasRole('ROLE_CUSTOM_CONVERTER')")
class CustomConverterResource(private val authenticationHolder: HmppsAuthenticationHolder) {

  @GetMapping("/active-caseload")
  fun getActiveCaseload() = CaseloadDetails((authenticationHolder.authentication as CustomAuthAwareAuthenticationToken).activeCaseload)

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

class CustomAuthAwareAuthenticationTokenConverter(private val activeCaseloadProvider: () -> String) : AuthAwareTokenConverter() {
  override fun convert(jwt: Jwt): CustomAuthAwareAuthenticationToken = super.convert(jwt)
    .let { authAwareAuthenticationToken ->
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
