package uk.justice.gov.hmpps.kotlin

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "hmpps.kotlin")
data class HmppsKotlinProperties(
  // TODO remove this and related tests when we have some real properties
  val foo: String = "bar",
) {
  init {
    fooMustBeBar()
  }

  private fun fooMustBeBar() {
    if (foo != "bar") throw InvalidHmppsKotlinPropertiesException("foo $foo is not bar")
  }
}

class InvalidHmppsKotlinPropertiesException(message: String) : IllegalStateException(message)
