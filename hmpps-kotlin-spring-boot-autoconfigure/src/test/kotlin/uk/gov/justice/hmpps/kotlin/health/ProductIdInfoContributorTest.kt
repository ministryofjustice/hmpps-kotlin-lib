package uk.gov.justice.hmpps.kotlin.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class ProductIdInfoContributorTest {
  private val runner =
    ApplicationContextRunner().withConfiguration(UserConfigurations.of(ProductIdInfoContributor::class.java))

  @Test
  fun `should be disabled if property set to false`() {
    runner.withPropertyValues("hmpps.info.productIdEnabled=false")
      .run { assertThat(it).doesNotHaveBean("productIdInfoContributor") }
  }

  @Test
  fun `should be disabled if property missing`() {
    runner.run { assertThat(it).doesNotHaveBean("productIdInfoContributor") }
  }

  @Test
  fun `should be enabled if property set`() {
    runner.withPropertyValues("hmpps.info.productIdEnabled=true")
      .run { assertThat(it).hasBean("productIdInfoContributor") }
  }
}
