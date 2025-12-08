package uk.gov.justice.hmpps.kotlin.sar

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.LocalDate

/**
 * Prisoners have the right to access and receive a copy of their personal data and other supplementary information.
 *
 * This is commonly referred to as a subject access request or ‘SAR’.
 */
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@RestController
@Tag(name = "Subject Access Request")
@PreAuthorize("@environment.containsProperty('hmpps.sar.additionalAccessRole') ? hasAnyRole('SAR_DATA_ACCESS', @environment.getProperty('hmpps.sar.additionalAccessRole')) : hasRole('SAR_DATA_ACCESS')")
@RequestMapping("/subject-access-request", produces = [MediaType.APPLICATION_JSON_VALUE])
@ConditionalOnBean(HmppsSubjectAccessRequestReactiveService::class)
class HmppsSubjectAccessRequestReactiveController(private val service: HmppsSubjectAccessRequestReactiveService) {

  @GetMapping
  @Operation(
    summary = "Provides content for a prisoner to satisfy the needs of a subject access request on their behalf",
    description = "Requires role SAR_DATA_ACCESS or additional role as specified by hmpps.sar.additionalAccessRole configuration.",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Request successfully processed - content found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = HmppsSubjectAccessRequestContent::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "204",
        description = "Request successfully processed - no content found",
      ),
      ApiResponse(
        responseCode = "209",
        description = "Subject Identifier is not recognised by this service",
      ),
      ApiResponse(
        responseCode = "401",
        description = "The client does not have authorisation to make this request",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "500",
        description = "Unexpected error occurred",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun getSarContentByReference(
    @RequestParam(name = "prn")
    @Parameter(description = "NOMIS Prison Reference Number")
    prn: String?,
    @RequestParam(name = "crn")
    @Parameter(description = "nDelius Case Reference Number")
    crn: String?,
    @RequestParam(value = "fromDate")
    @Parameter(description = "Optional parameter denoting minimum date of event occurrence which should be returned in the response")
    fromDate: LocalDate?,
    @RequestParam(value = "toDate")
    @Parameter(description = "Optional parameter denoting maximum date of event occurrence which should be returned in the response")
    toDate: LocalDate?,
  ): ResponseEntity<Any> {
    if (prn.isNullOrBlank() && crn.isNullOrBlank()) {
      return ResponseEntity.badRequest().body(
        ErrorResponse(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "One of prn or crn must be supplied.",
          developerMessage = "One of prn or crn must be supplied.",
        ),
      )
    }

    val content = if (service is HmppsPrisonSubjectAccessRequestReactiveService && !prn.isNullOrBlank()) {
      service.getPrisonContentFor(prn, fromDate, toDate)
    } else if (service is HmppsProbationSubjectAccessRequestReactiveService && !crn.isNullOrBlank()) {
      service.getProbationContentFor(crn, fromDate, toDate)
    } else if (service is HmppsPrisonProbationSubjectAccessRequestReactiveService) {
      service.getContentFor(prn, crn, fromDate, toDate)
    } else {
      return ResponseEntity.status(209).build()
    }

    return content?.let { ResponseEntity.ok(it) } ?: ResponseEntity.noContent().build()
  }
}
