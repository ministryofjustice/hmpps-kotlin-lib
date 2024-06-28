package uk.gov.justice.hmpps.kotlin.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "hmpps", name = ["info.product-id-enabled"])
class ProductIdInfoContributor(@Value("\${product-id:default}") private val productId: String) : InfoContributor {

  override fun contribute(builder: Info.Builder) {
    builder.withDetail("productId", productId)
  }
}
