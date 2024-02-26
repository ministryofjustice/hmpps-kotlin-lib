package uk.gov.justice.digital.hmpps.testappreactive.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.health.ReactiveHealthCheck

@Component("hmppsAuthApi")
class OAuthApiHealth(@Qualifier("hmppsAuthHealthWebClient") webClient: WebClient) : ReactiveHealthCheck(webClient)

@Component("prisonApi")
class PrisonApiHealth(@Qualifier("prisonApiHealthWebClient") webClient: WebClient) : ReactiveHealthCheck(webClient)
