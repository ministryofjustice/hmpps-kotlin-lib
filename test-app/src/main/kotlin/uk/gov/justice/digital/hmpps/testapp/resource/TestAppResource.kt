package uk.gov.justice.digital.hmpps.testapp.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
class TestAppResource {

  @PreAuthorize("hasRole('ROLE_TEST_APP')")
  @RequestMapping("/time")
  fun getTime() = LocalDateTime.now()
}
