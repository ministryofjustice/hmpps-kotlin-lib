package uk.gov.justice.hmpps.kotlin.auth.dsl

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import reactor.core.publisher.Mono
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareAuthenticationToken
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareReactiveTokenConverter
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareTokenConverter

@DslMarker
annotation class Oauth2CustomizerDslMarker

/**
 * Part of the [ResourceServerConfigurationCustomizerDsl] DSL.
 *
 * To create a new instance of [Oauth2Customizer], use the [ResourceServerConfigurationCustomizer.Companion.invoke] method, e.g.
 *
 * ```
 *   @Bean
 *   fun resourceServerCustomizer() = ResourceServerConfigurationCustomizer {
 *     oauth2 { tokenConverter = CustomTokenConverter() }
 *   }
 * ```
 *
 * Note that by default you get an AuthAwareTokenConverter (or AuthAwareReactiveTokenConverter for reactive applications)
 * so generally there is no need to override the defaults.
 */
@Oauth2CustomizerDslMarker
interface Oauth2CustomizerDsl {
  /**
   * Overrides the default AuthAwareTokenConverter for a non-reactive servlet based application.
   *
   * Note that we recommend extending AuthAwareAuthenticationToken and AuthAwareTokenConverter if HMPPS Auth is your
   * authentication provider.
   */
  var tokenConverter: Converter<Jwt, AbstractAuthenticationToken>?

  /**
   * Overrides the default AuthAwareReactiveTokenConverter for a reactive application
   *
   * Note that the tokens produced must extend AuthAwareAuthenticationToken.
   */
  var reactiveTokenConverter: Converter<Jwt, Mono<AuthAwareAuthenticationToken>>?
}

class Oauth2Customizer(
  val tokenConverter: Converter<Jwt, AbstractAuthenticationToken>,
  val reactiveTokenConverter: Converter<Jwt, Mono<AuthAwareAuthenticationToken>>,
)

class Oauth2CustomizerBuilder : Oauth2CustomizerDsl {
  override var tokenConverter: Converter<Jwt, AbstractAuthenticationToken>? = null
  override var reactiveTokenConverter: Converter<Jwt, Mono<AuthAwareAuthenticationToken>>? = null

  fun build(): Oauth2Customizer = Oauth2Customizer(
    tokenConverter ?: AuthAwareTokenConverter(),
    reactiveTokenConverter ?: AuthAwareReactiveTokenConverter(),
  )
}
