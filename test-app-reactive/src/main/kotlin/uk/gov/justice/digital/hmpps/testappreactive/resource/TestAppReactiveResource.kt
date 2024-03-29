package uk.gov.justice.digital.hmpps.testappreactive.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.testappreactive.service.PrisonApiService
import java.time.LocalDateTime

@RestController
class TestAppReactiveResource(val prisonApiService: PrisonApiService) {

  @PreAuthorize("hasRole('ROLE_TEST_APP_REACTIVE')")
  @RequestMapping("/time")
  suspend fun getTime() = LocalDateTime.now()

  @PreAuthorize("hasRole('ROLE_TEST_APP_REACTIVE')")
  @RequestMapping("/prisoner/{prisonNumber}/booking")
  suspend fun getOffenderBookingId(@PathVariable prisonNumber: String) = prisonApiService.getOffenderBooking(prisonNumber)
}
