package uk.gov.justice.digital.hmpps.testappreactive.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class PrisonApiService(val prisonApiWebClient: WebClient) {

  fun getOffender(offenderNo: String): OffenderBooking? = prisonApiWebClient.get()
    .uri("/api/offender/{offenderNo}", offenderNo)
    .retrieve()
    .bodyToMono(OffenderBooking::class.java)
    .onErrorResume(WebClientResponseException.NotFound::class.java) { Mono.empty() }
    .block()
}

data class OffenderBooking(val bookingId: Long)
