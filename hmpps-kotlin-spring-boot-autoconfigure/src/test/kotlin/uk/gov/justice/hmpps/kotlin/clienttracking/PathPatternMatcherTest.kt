package uk.gov.justice.hmpps.kotlin.clienttracking

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.server.RequestPath

class PathPatternMatcherTest {

  @Test
  fun `should not match if no include paths defined`() {
    val matcher = PathPatternMatcher()

    assertThat(matcher.matches(RequestPath.parse("/api/something", ""))).isFalse
  }

  @Test
  fun `should match include path`() {
    val matcher = PathPatternMatcher(includePaths = listOf("/api/**"))

    assertThat(matcher.matches(RequestPath.parse("/api/something", ""))).isTrue()
  }

  @Test
  fun `should not match if missing from include path`() {
    val matcher = PathPatternMatcher(includePaths = listOf("/api/**"))

    assertThat(matcher.matches(RequestPath.parse("/not-api/something", ""))).isFalse
  }

  @Test
  fun `should not match if in exclude path`() {
    val matcher = PathPatternMatcher(includePaths = listOf("/**"), excludePaths = listOf("/api/**"))

    assertThat(matcher.matches(RequestPath.parse("/api/something", ""))).isFalse
  }

  @Test
  fun `should match if missing from exclude path`() {
    val matcher = PathPatternMatcher(includePaths = listOf("/**"), excludePaths = listOf("/not-api/**"))

    assertThat(matcher.matches(RequestPath.parse("/api/something", ""))).isTrue
  }
}
