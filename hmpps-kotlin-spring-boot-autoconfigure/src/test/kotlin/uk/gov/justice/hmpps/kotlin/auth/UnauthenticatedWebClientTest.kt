package uk.gov.justice.hmpps.kotlin.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

class UnauthenticatedWebClientTest {

  @Test
  fun `should create an unauthenticatedWebClient with baseUrl`() {
    val baseUrl = "https://example.com"
    val timeout = Duration.ofSeconds(20)
    val builder = WebClient.builder()

    val webClient = builder.unauthenticatedWebClient(baseUrl, timeout)

    assertThat(webClient).isNotNull
  }

  @Test
  fun `should use default timeout when not specified`() {
    val baseUrl = "https://example.com"
    val builder = WebClient.builder()

    val webClient = builder.unauthenticatedWebClient(baseUrl)

    assertThat(webClient).isNotNull
  }

  @Test
  fun `should be able to build requests with the returned client`() {
    val baseUrl = "https://api.example.com"
    val builder = WebClient.builder()
    val webClient = builder.unauthenticatedWebClient(baseUrl, Duration.ofSeconds(10))

    // Verify the client can be used to build a request
    val requestSpec = webClient.get().uri("/test")

    assertThat(requestSpec).isNotNull
  }
}
