package uk.gov.justice.hmpps.kotlin.customize

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ResourceServerConfigurationCustomizerTest {

  @Nested
  inner class UnauthorizedRequestPaths {
    @Test
    fun `should default unauthorized request paths`() {
      val customizer = ResourceServerConfigurationCustomizer.build {}

      assertThat(customizer.unauthorizedRequestPathsCustomizer.unauthorizedRequestPaths).contains(
        "/health/**",
        "/info",
        "/v3/api-docs/**",
      )
    }

    @Test
    fun `should ignore default unauthorized request paths`() {
      val customizer = ResourceServerConfigurationCustomizer.build {
        unauthorizedRequestPaths(
          includeDefaults = false,
        )
      }

      assertThat(customizer.unauthorizedRequestPathsCustomizer.unauthorizedRequestPaths).isEmpty()
    }

    @Test
    fun `should add additional unauthorized request paths`() {
      val customizer = ResourceServerConfigurationCustomizer.build {
        unauthorizedRequestPaths(
          addPaths = setOf("/some-path"),
        )
      }

      assertThat(customizer.unauthorizedRequestPathsCustomizer.unauthorizedRequestPaths).contains(
        "/health/**",
        "/some-path",
      )
    }
  }
}
