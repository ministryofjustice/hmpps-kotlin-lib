package uk.gov.justice.hmpps.kotlin

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(HmppsKotlinProperties::class)
@AutoConfigureBefore(WebFluxAutoConfiguration::class, WebMvcAutoConfiguration::class)
class HmppsKotlinConfiguration
