package uk.gov.justice.digital.hmpps.testapp.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.testapp.service.PrisonApiService
import java.time.LocalDateTime

@RestController
@PreAuthorize("hasRole('ROLE_TEST_APP')")
class TestAppResource(private val prisonApiService: PrisonApiService) {

  @RequestMapping("/time")
  fun getTime(): LocalDateTime = LocalDateTime.now()

  @RequestMapping("/prisoner/{prisonNumber}/booking")
  fun getOffenderBooking(@PathVariable prisonNumber: String) = prisonApiService.getOffenderBooking(prisonNumber)

  @RequestMapping("/auth/token")
  fun getAuthToken() = prisonApiService.getAuthToken()
}
