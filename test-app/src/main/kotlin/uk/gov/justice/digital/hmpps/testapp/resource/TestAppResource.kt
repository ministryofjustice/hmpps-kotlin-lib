package uk.gov.justice.digital.hmpps.testapp.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.testapp.service.PrisonApiService
import java.time.LocalDateTime

@RestController
class TestAppResource(val prisonApiService: PrisonApiService) {

  @PreAuthorize("hasRole('ROLE_TEST_APP')")
  @RequestMapping("/time")
  fun getTime() = LocalDateTime.now()

  @RequestMapping("/offender/{id}/booking")
  fun getOffenderBookingId(@PathVariable id: String) = prisonApiService.getOffender(id)
}
