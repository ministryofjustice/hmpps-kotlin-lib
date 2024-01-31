package uk.justice.gov.hmpps.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HmppsKotlinPropertiesTest {

  @Test
  fun `foo should default to bar`() {
    val properties = HmppsKotlinProperties()
    assertThat(properties.foo).isEqualTo("bar")
  }

  @Test
  fun `foo must be bar`() {
    assertThrows<InvalidHmppsKotlinPropertiesException> { HmppsKotlinProperties(foo = "foo") }
      .also {
        assertThat(it).hasMessage("foo foo is not bar")
      }
  }
}
