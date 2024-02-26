package uk.gov.justice.hmpps.kotlin.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

abstract class HealthCheck(private val webClient: WebClient) : HealthIndicator {
  override fun health(): Health = webClient.health()
    .block() ?: Health.down().withDetail("HttpStatus", "No response returned from ping").build()
}
abstract class ReactiveHealthCheck(private val webClient: WebClient) : ReactiveHealthIndicator {
  override fun health(): Mono<Health> = webClient.health()
}

private fun WebClient.health(): Mono<Health> = get()
  .uri("/health/ping")
  .retrieve()
  .toEntity(String::class.java)
  .flatMap { Mono.just(Health.up().withDetail("HttpStatus", it?.statusCode).build()) }
  .onErrorResume(WebClientResponseException::class.java) {
    Mono.just(
      Health.down(it).withDetail("body", it.responseBodyAsString).withDetail("HttpStatus", it.statusCode).build(),
    )
  }
  .onErrorResume(Exception::class.java) { Mono.just(Health.down(it).build()) }
