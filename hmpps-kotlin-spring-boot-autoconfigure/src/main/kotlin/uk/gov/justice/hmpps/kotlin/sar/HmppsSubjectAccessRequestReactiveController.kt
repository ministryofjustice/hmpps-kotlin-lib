package uk.gov.justice.hmpps.kotlin.sar

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
@PreAuthorize("hasAnyRole('SAR_DATA_ACCESS', @environment.getProperty('hmpps.sar.additionalAccessRole', 'SAR_DATA_ACCESS'))")
@RequestMapping("/subject-access-request", produces = [MediaType.APPLICATION_JSON_VALUE])
@ConditionalOnBean(HmppsSubjectAccessRequestReactiveService::class)
class HmppsSubjectAccessRequestReactiveController(
  private val service: HmppsSubjectAccessRequestReactiveService,
  @Value("\${subject-access-request.template-path:}") private val subjectAccessRequestTemplatePath: String,
) {

  private companion object {
    private val LOG = LoggerFactory.getLogger(HmppsSubjectAccessRequestReactiveController::class.java)
  }

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

  @GetMapping("/template")
  fun getServiceTemplate(): ResponseEntity<Any> = if (subjectAccessRequestTemplatePath.isBlank()) {
    LOG.warn("subject-access-request.template-path configuration value is blank")

    ResponseEntity.internalServerError().body(
      ErrorResponse(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        userMessage = "A subject access request mustache template has not been configured for this service.",
        developerMessage = "A subject access request mustache template has not been configured for this service.",
      ),
    )
  } else {
    HmppsSubjectAccessRequestController::class.java.getResourceAsStream(subjectAccessRequestTemplatePath)
      ?.let { ResponseEntity(it.readAllBytes(), HttpStatus.OK) }
      ?: run {
        LOG.warn("subject-access-request.template-path: '{}' file not found", subjectAccessRequestTemplatePath)

        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
          ErrorResponse(
            status = HttpStatus.NOT_FOUND,
            userMessage = "Configured subject access request mustache template not found",
            developerMessage = "Configured subject access request mustache template not found",
          ),
        )
      }
  }
}
