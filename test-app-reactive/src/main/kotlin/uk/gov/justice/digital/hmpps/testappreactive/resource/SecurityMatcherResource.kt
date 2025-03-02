package uk.gov.justice.digital.hmpps.testappreactive.resource

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SecurityMatcherResource {

  // This endpoint is secured by the `customSecurityMatcherCustomizer` in `SecurityMatcherTest.CustomizerConfiguration`
  @GetMapping("/protected-by-custom-security-matcher")
  suspend fun getSecurityMatcherEndpoint(): String = "OK"
}
