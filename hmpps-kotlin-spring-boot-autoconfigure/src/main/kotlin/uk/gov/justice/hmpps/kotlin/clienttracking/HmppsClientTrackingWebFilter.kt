package uk.gov.justice.hmpps.kotlin.clienttracking

import org.springframework.http.HttpHeaders
import org.springframework.http.server.RequestPath
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono

class HmppsClientTrackingWebFilter(
  includePaths: List<String> = listOf("/**"),
  excludePaths: List<String> = listOf(),
  val setTrackingDetails: SetTrackingDetails = Any::defaultTrackingDetails,
) : WebFilter {
  private val pathMatcher = PathPatternMatcher(includePaths, excludePaths)

  override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
    if (!pathMatcher.matches(exchange.request.path)) {
      return chain.filter(exchange)
    }

    exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
      ?.also { token -> setTrackingDetails(token) }

    return chain.filter(exchange)
  }
}

internal class PathPatternMatcher(
  includePaths: List<String> = listOf(),
  excludePaths: List<String> = listOf(),
) {
  private val includePathPatterns = includePaths.map { PathPatternParser.defaultInstance.parse(it) }
  private val excludePathPatterns = excludePaths.map { PathPatternParser.defaultInstance.parse(it) }

  fun matches(requestPath: RequestPath): Boolean {
    if (excludePathPatterns.any { it.matches(requestPath) }) {
      return false
    }
    if (includePathPatterns.none { it.matches(requestPath) }) {
      return false
    }
    return true
  }
}
