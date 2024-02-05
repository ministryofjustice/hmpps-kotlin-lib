package uk.gov.justice.digital.hmpps.testappreactive.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
class TestAppReactiveResource {

  @PreAuthorize("hasRole('ROLE_TEST_APP_REACTIVE')")
  @RequestMapping("/time")
  suspend fun getTime() = LocalDateTime.now()
}
