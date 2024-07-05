package uk.gov.justice.hmpps.test.kotlin.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner
import org.springframework.boot.test.context.runner.WebApplicationContextRunner

class JwtAuthorisationHelperTest {
  @Nested
  inner class Servlet {
    private val runner =
      WebApplicationContextRunner().withConfiguration(UserConfigurations.of(JwtAuthorisationHelper::class.java))

    @Test
    fun `should be disabled if property set to false`() {
      runner.withPropertyValues(
        "hmpps.test.jwtHelperEnabled=false",
      )
        .run { assertThat(it).doesNotHaveBean("jwtDecoder") }
    }

    @Test
    fun `should be enabled if property missing`() {
      runner.withPropertyValues()
        .run { assertThat(it).hasBean("jwtDecoder") }
    }

    @Test
    fun `should be enabled if property set`() {
      runner.withPropertyValues(
        "hmpps.test.jwtHelperEnabled=true",
      )
        .run { assertThat(it).hasBean("jwtDecoder") }
    }
  }

  @Nested
  inner class Reactive {
    private val runner =
      ReactiveWebApplicationContextRunner().withConfiguration(UserConfigurations.of(JwtAuthorisationHelper::class.java))

    @Test
    fun `should be disabled if property set to false`() {
      runner.withPropertyValues(
        "hmpps.test.jwtHelperEnabled=false",
      )
        .run { assertThat(it).doesNotHaveBean("reactiveJwtDecoder") }
    }

    @Test
    fun `should be enabled if property missing`() {
      runner.withPropertyValues()
        .run { assertThat(it).hasBean("reactiveJwtDecoder") }
    }

    @Test
    fun `should be enabled if property set`() {
      runner.withPropertyValues(
        "hmpps.test.jwtHelperEnabled=true",
      )
        .run { assertThat(it).hasBean("reactiveJwtDecoder") }
    }
  }
}
