package uk.gov.justice.digital.hmpps.testappreactive.integration.auth

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey

@Import(JwtAuthorisationHelperBeanTest.CustomizerConfiguration::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["hmpps.test.jwt-helper-enabled=false"],
)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class JwtAuthorisationHelperBeanTest {
  @Autowired
  lateinit var webTestClient: WebTestClient

  @TestConfiguration
  class CustomizerConfiguration {
    private val keyPair: KeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

    // Deliberately define our own JWT Decoder even though there is one in the test lib
    // this will fail if there are two jwtDecoder beans
    @Bean
    @Primary
    fun reactiveJwtDecoder(): ReactiveJwtDecoder = NimbusReactiveJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()
  }

  @Test
  fun `should return okay`() {
    // go for an unprotected endpoint otherwise we would have to use the jwt decoder above to create a token for
    // ourselves and effectively rewrite the whole JwtAuthorisationHelper here anyway.
    webTestClient.get()
      .uri("/unprotected/auth/token")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("greeting").isEqualTo("No authentication provided")
  }
}
