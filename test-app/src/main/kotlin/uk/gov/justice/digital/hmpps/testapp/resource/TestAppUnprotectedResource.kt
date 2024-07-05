package uk.gov.justice.digital.hmpps.testapp.resource

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.testapp.service.PrisonApiService

@RestController
@RequestMapping("/unprotected")
class TestAppUnprotectedResource(private val prisonApiService: PrisonApiService) {
  @RequestMapping("/auth/token")
  fun getAuthToken() = prisonApiService.getAuthTokenOrNull()
}
