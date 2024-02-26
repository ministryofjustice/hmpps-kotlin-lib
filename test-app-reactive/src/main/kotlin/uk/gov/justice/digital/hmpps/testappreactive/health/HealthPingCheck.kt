package uk.gov.justice.digital.hmpps.testappreactive.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.health.ReactiveHealthPingCheck

@Component("hmppsAuth")
class HmppsAuthHealthPing(@Qualifier("hmppsAuthHealthWebClient") webClient: WebClient) : ReactiveHealthPingCheck(webClient)

@Component("prisonApi")
class PrisonApiHealthPing(@Qualifier("prisonApiHealthWebClient") webClient: WebClient) : ReactiveHealthPingCheck(webClient)
