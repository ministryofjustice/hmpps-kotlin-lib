package uk.gov.justice.hmpps.kotlin.clienttracking

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.web.servlet.HandlerInterceptor

class HmppsClientTrackingInterceptor(
  val includePaths: List<String> = listOf("/**"),
  val excludePaths: List<String> = listOf(),
  val setTrackingDetails: SetTrackingDetails = Any::defaultTrackingDetails,
) : HandlerInterceptor {
  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    request.getHeader(HttpHeaders.AUTHORIZATION)
      ?.also { token -> setTrackingDetails(token) }

    return true
  }
}
