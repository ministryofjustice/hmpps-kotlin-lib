package uk.gov.justice.digital.hmpps.testappreactive.resource

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.testappreactive.service.PrisonApiService

@RestController
@RequestMapping("/unprotected")
class TestAppUnprotectedResource(private val prisonApiService: PrisonApiService) {
  @RequestMapping("/auth/token")
  suspend fun getAuthToken() = prisonApiService.getAuthTokenOrNull()
}
