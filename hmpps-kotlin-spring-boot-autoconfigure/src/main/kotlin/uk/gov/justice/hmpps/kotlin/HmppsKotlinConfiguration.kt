package uk.gov.justice.hmpps.kotlin

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.gov.justice.hmpps.kotlin.auth.HmppsReactiveResourceServerConfiguration
import uk.gov.justice.hmpps.kotlin.auth.HmppsResourceServerConfiguration
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestController

@Configuration
@AutoConfigureBefore(WebFluxAutoConfiguration::class, WebMvcAutoConfiguration::class)
@Import(
  HmppsResourceServerConfiguration::class,
  HmppsReactiveResourceServerConfiguration::class,
  HmppsSubjectAccessRequestController::class,
)
class HmppsKotlinConfiguration
