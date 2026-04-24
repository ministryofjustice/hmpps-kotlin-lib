package uk.gov.justice.hmpps.kotlin.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Properties

class HmppsWebClientConfigurationTest {

  @Test
  fun `should convert java non proxy hosts syntax to reactor regex`() {
    val result = toReactorNonProxyHostsPattern("localhost|127.*|envoy-https-proxy|*.svc|*.cluster.local")

    assertThat(result).isEqualTo("^localhost$|^127\\..*$|^envoy-https-proxy$|^.*\\.svc$|^.*\\.cluster\\.local$")
  }

  @Test
  fun `should ignore blank java non proxy host entries`() {
    val result = toReactorNonProxyHostsPattern(" localhost | | *.svc ")

    assertThat(result).isEqualTo("^localhost$|^.*\\.svc$")
  }

  @Test
  fun `should return null when java non proxy hosts is blank`() {
    assertThat(toReactorNonProxyHostsPattern(" ")).isNull()
    assertThat(toReactorNonProxyHostsPattern(null)).isNull()
  }

  @Test
  fun `should convert no proxy env syntax to reactor regex`() {
    val result = toReactorNoProxyHostsPattern("localhost,127.0.0.1,envoy-https-proxy,.svc,.cluster.local")

    assertThat(result).isEqualTo("^localhost$|^127\\.0\\.0\\.1$|^envoy-https-proxy$|^.*\\.svc$|^.*\\.cluster\\.local$")
  }

  @Test
  fun `should ignore blank no proxy entries`() {
    val result = toReactorNoProxyHostsPattern(" localhost , , .svc ")

    assertThat(result).isEqualTo("^localhost$|^.*\\.svc$")
  }

  @Test
  fun `should prefer proxy environment variables`() {
    val result = resolveProxyConfiguration(
      environment = mapOf(
        "HTTPS_PROXY" to "http://envoy-https-proxy:3128",
        "NO_PROXY" to "localhost,.svc",
      ),
      systemProperties = Properties().apply {
        setProperty("https.proxyHost", "ignored-proxy")
        setProperty("https.proxyPort", "1234")
      },
    )

    assertThat(result).isEqualTo(
      ProxyConfiguration(
        host = "envoy-https-proxy",
        port = 3128,
        nonProxyHostsPattern = "^localhost$|^.*\\.svc$",
      ),
    )
  }

  @Test
  fun `should fall back to system properties when proxy environment variables are missing`() {
    val result = resolveProxyConfiguration(
      environment = emptyMap(),
      systemProperties = Properties().apply {
        setProperty("https.proxyHost", "envoy-https-proxy")
        setProperty("https.proxyPort", "3128")
        setProperty("https.nonProxyHosts", "localhost|*.svc")
      },
    )

    assertThat(result).isEqualTo(
      ProxyConfiguration(
        host = "envoy-https-proxy",
        port = 3128,
        nonProxyHostsPattern = "^localhost$|^.*\\.svc$",
      ),
    )
  }

  @Test
  fun `should fall back to system non proxy hosts when no proxy env var is missing`() {
    val result = resolveProxyConfiguration(
      environment = mapOf("HTTPS_PROXY" to "http://envoy-https-proxy:3128"),
      systemProperties = Properties().apply {
        setProperty("https.nonProxyHosts", "localhost|*.svc")
      },
    )

    assertThat(result).isEqualTo(
      ProxyConfiguration(
        host = "envoy-https-proxy",
        port = 3128,
        nonProxyHostsPattern = "^localhost$|^.*\\.svc$",
      ),
    )
  }
}
