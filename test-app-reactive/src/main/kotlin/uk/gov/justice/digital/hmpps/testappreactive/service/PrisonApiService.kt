package uk.gov.justice.digital.hmpps.testappreactive.service

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class PrisonApiService(val prisonApiWebClient: WebClient) {

  suspend fun getOffenderBooking(prisonNumber: String): OffenderBooking? = prisonApiWebClient.get()
    // Note that we don't use string interpolation here ("/$prisonNumber"). This is important - using string interpolation causes each uri to be added as a separate path in app insights and you'll run out of memory in your app
    .uri("/api/offender/{prisonNumber}", prisonNumber)
    .retrieve()
    .bodyToMono(OffenderBooking::class.java)
    .onErrorResume(WebClientResponseException.NotFound::class.java) { Mono.empty() }
    .awaitSingleOrNull()
}

data class OffenderBooking(val bookingId: Long)
