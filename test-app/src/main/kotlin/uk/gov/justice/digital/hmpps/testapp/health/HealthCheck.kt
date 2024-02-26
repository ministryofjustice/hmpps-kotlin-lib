package uk.gov.justice.digital.hmpps.testapp.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.health.HealthCheck

@Component("hmppsAuthApi")
class OAuthApiHealth(@Qualifier("hmppsAuthHealthWebClient") webClient: WebClient) : HealthCheck(webClient)

@Component("prisonApi")
class PrisonApiHealth(@Qualifier("prisonApiHealthWebClient") webClient: WebClient) : HealthCheck(webClient)
