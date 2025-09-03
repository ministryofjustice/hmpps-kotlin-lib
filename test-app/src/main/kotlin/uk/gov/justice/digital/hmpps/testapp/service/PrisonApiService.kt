package uk.gov.justice.digital.hmpps.testapp.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@Service
class PrisonApiService(
  private val prisonApiWebClient: WebClient,
  private val usernameAwarePrisonApiWebClient: WebClient,
  private val hmppsAuthenticationHolder: HmppsAuthenticationHolder,
) {

  fun getOffenderBooking(prisonNumber: String): OffenderBooking? = prisonApiWebClient.get()
    // Note that we don't use string interpolation here ("/$prisonNumber").
    // This is important - using string interpolation causes each uri to be added as a separate path in app insights and
    // you'll run out of memory in your app
    .uri("/api/offender/{prisonNumber}", prisonNumber)
    .retrieve()
    .bodyToMono(OffenderBooking::class.java)
    .onErrorResume(WebClientResponseException.NotFound::class.java) { Mono.empty() }
    .block()

  fun getOffenderBookingWithUserInContext(prisonNumber: String): OffenderBooking? = usernameAwarePrisonApiWebClient.get()
    .uri("/api/offender/{prisonNumber}", prisonNumber)
    .retrieve()
    .bodyToMono(OffenderBooking::class.java)
    .onErrorResume(WebClientResponseException.NotFound::class.java) { Mono.empty() }
    .block()

  fun getAuthToken() = AuthResponse("Hello there ${hmppsAuthenticationHolder.principal}")

  fun getAuthTokenOrNull() = hmppsAuthenticationHolder.authenticationOrNull?.let {
    AuthResponse("Hello there ${it.principal}")
  } ?: AuthResponse("No authentication provided")
}

data class OffenderBooking(val bookingId: Long)

data class AuthResponse(val greeting: String)
