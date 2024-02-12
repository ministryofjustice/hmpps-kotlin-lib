package uk.gov.justice.hmpps.kotlin.auth.dsl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ResourceServerConfigurationCustomizerTest {

  @Nested
  inner class UnauthorizedRequestPaths {
    @Test
    fun `should default unauthorized request paths`() {
      val customizer = ResourceServerConfigurationCustomizer {}

      assertThat(customizer.unauthorizedRequestPathsCustomizer.unauthorizedRequestPaths).contains(
        "/health/**",
        "/info",
        "/v3/api-docs/**",
      )
    }

    @Test
    fun `should ignore default unauthorized request paths`() {
      val customizer = ResourceServerConfigurationCustomizer {
        unauthorizedRequestPaths {
          includeDefaults = false
        }
      }

      assertThat(customizer.unauthorizedRequestPathsCustomizer.unauthorizedRequestPaths).isEmpty()
    }

    @Test
    fun `should add additional unauthorized request paths`() {
      val customizer = ResourceServerConfigurationCustomizer {
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
      val customizer = ResourceServerConfigurationCustomizer {}

      assertThat(customizer.anyRequestRoleCustomizer.defaultRole).isNull()
    }

    @Test
    fun `should set default role`() {
      val customizer = ResourceServerConfigurationCustomizer {
        anyRequestRole {
          defaultRole = "ROLE_MY_ROLE"
        }
      }

      assertThat(customizer.anyRequestRoleCustomizer.defaultRole).isEqualTo("ROLE_MY_ROLE")
    }
  }

  @Nested
  inner class AuthorizeHttpRequests {
    @Test
    fun `should default to null`() {
      val customizer = ResourceServerConfigurationCustomizer {}

      assertThat(customizer.authorizeHttpRequestsCustomizer.dsl).isNull()
    }

    @Test
    fun `should save authorizeHttpRequests DSL`() {
      val customizer = ResourceServerConfigurationCustomizer {
        authorizeHttpRequests {
          authorize("/anything", permitAll)
        }
      }

      assertThat(customizer.authorizeHttpRequestsCustomizer.dsl).isNotNull
    }

    @Test
    fun `should not allow authorizeHttpRequests with any request role customization`() {
      assertThrows<IllegalStateException> {
        ResourceServerConfigurationCustomizer {
          authorizeHttpRequests {
            authorize("/anything", permitAll)
          }
          anyRequestRole {
            defaultRole = "ROLE_MY_ROLE"
          }
        }
      }
    }

    @Test
    fun `should not allow authorizeHttpRequests with unauthorized request paths customization`() {
      assertThrows<IllegalStateException> {
        ResourceServerConfigurationCustomizer {
          authorizeHttpRequests {
            authorize("/anything", permitAll)
          }
          unauthorizedRequestPaths {
            includeDefaults = false
          }
        }
      }.also {
        assertThat(it.message).contains("authorizeHttpRequests")
      }
    }
  }

  @Nested
  inner class AuthorizeExchange {
    @Test
    fun `should default to null`() {
      val customizer = ResourceServerConfigurationCustomizer {}

      assertThat(customizer.authorizeExchangeCustomizer.dsl).isNull()
    }

    @Test
    fun `should save authorizeExchange DSL`() {
      val customizer = ResourceServerConfigurationCustomizer {
        authorizeExchange {
          authorize("/anything", permitAll)
        }
      }

      assertThat(customizer.authorizeExchangeCustomizer.dsl).isNotNull
    }

    @Test
    fun `should not allow authorizeExchange with any request role customization`() {
      assertThrows<IllegalStateException> {
        ResourceServerConfigurationCustomizer {
          authorizeExchange {
            authorize("/anything", permitAll)
          }
          anyRequestRole {
            defaultRole = "ROLE_MY_ROLE"
          }
        }
      }
    }

    @Test
    fun `should not allow authorizeExchange with unauthorized request paths customization`() {
      assertThrows<IllegalStateException> {
        ResourceServerConfigurationCustomizer {
          authorizeExchange {
            authorize("/anything", permitAll)
          }
          unauthorizedRequestPaths {
            includeDefaults = false
          }
        }
      }.also {
        assertThat(it.message).contains("authorizeExchange")
      }
    }
  }

  @Nested
  inner class Validation {
    @Test
    fun `should not allow authorizeHttpRequests and authorizeExchange together`() {
      assertThrows<IllegalStateException> {
        ResourceServerConfigurationCustomizer {
          authorizeExchange {
            authorize("/anything", permitAll)
          }
          authorizeHttpRequests {
            authorize("/anything", permitAll)
          }
        }
      }.also {
        assertThat(it.message).contains("authorizeHttpRequests").contains("authorizeExchange")
      }
    }
  }
}
