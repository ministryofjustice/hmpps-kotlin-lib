package uk.gov.justice.digital.hmpps.testappreactive.service

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.hmpps.kotlin.auth.HmppsReactiveAuthenticationHolder

@Service
class PrisonApiService(
  private val prisonApiWebClient: WebClient,
  private val hmppsAuthenticationHolder: HmppsReactiveAuthenticationHolder,
) {

  suspend fun getOffenderBooking(prisonNumber: String): OffenderBooking? = prisonApiWebClient.get()
    // Note that we don't use string interpolation here ("/$prisonNumber").
    // This is important - using string interpolation causes each uri to be added as a separate path in app insights and
    // you'll run out of memory in your app
    .uri("/api/offender/{prisonNumber}", prisonNumber)
    .retrieve()
    .bodyToMono(OffenderBooking::class.java)
    .onErrorResume(WebClientResponseException.NotFound::class.java) { Mono.empty() }
    .awaitSingleOrNull()

  suspend fun getAuthToken() = AuthResponse("Hello there ${hmppsAuthenticationHolder.getPrincipal()}")

  suspend fun getAuthTokenOrNull() = hmppsAuthenticationHolder.getAuthenticationOrNull()?.let {
    AuthResponse("Hello there ${it.principal}")
  } ?: AuthResponse("Not sure why I allowed you in?")
}

data class OffenderBooking(val bookingId: Long)

data class AuthResponse(val greeting: String)
