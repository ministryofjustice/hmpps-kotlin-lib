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
        unauthorizedRequestPaths {
          includeDefaults = false
        }
      }

      assertThat(customizer.unauthorizedRequestPathsCustomizer.unauthorizedRequestPaths).isEmpty()
    }

    @Test
    fun `should add additional unauthorized request paths`() {
      val customizer = ResourceServerConfigurationCustomizer.build {
        unauthorizedRequestPaths {
          addPaths = setOf("/some-path")
        }
      }

      assertThat(customizer.unauthorizedRequestPathsCustomizer.unauthorizedRequestPaths).contains(
        "/health/**",
        "/some-path",
      )
    }
  }

  @Nested
  inner class AnyRequestRole {
    @Test
    fun `should default to no role`() {
      val customizer = ResourceServerConfigurationCustomizer.build {}

      assertThat(customizer.anyRequestRoleCustomizer.defaultRole).isNull()
    }

    @Test
    fun `should set default role`() {
      val customizer = ResourceServerConfigurationCustomizer.build {
        anyRequestRole {
          defaultRole = "ROLE_MY_ROLE"
        }
      }

      assertThat(customizer.anyRequestRoleCustomizer.defaultRole).isEqualTo("ROLE_MY_ROLE")
    }
  }
}
