package uk.gov.justice.hmpps.kotlin.sar

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@RestController
@Tag(name = "Subject Access Request")
@PreAuthorize("hasAnyRole('SAR_DATA_ACCESS', @environment.getProperty('hmpps.sar.additionalAccessRole', 'SAR_DATA_ACCESS'))")
@RequestMapping("/subject-access-request/template", produces = [MediaType.TEXT_PLAIN_VALUE])
@ConditionalOnBean(HmppsSubjectAccessRequestReactiveService::class)
@ConditionalOnBooleanProperty(value = ["hmpps.sar.template.enabled"], havingValue = true)
class HmppsSubjectAccessRequestReactiveTemplateController(
  @Value("\${hmpps.sar.template.path:}") private val subjectAccessRequestTemplatePath: String,
) {

  protected companion object {
    private val LOG = LoggerFactory.getLogger(HmppsSubjectAccessRequestReactiveTemplateController::class.java)
  }

  @PostConstruct
  protected fun validateTemplateConfiguration() {
    if (subjectAccessRequestTemplatePath.isBlank()) {
      throw sarTemplateConfigurationMissingException()
    }

    if (!ClassPathResource(subjectAccessRequestTemplatePath).exists()) {
      throw sarTemplateConfigurationInvalidException()
    }
  }

  @GetMapping
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Request successfully processed - return template file content",
        content = [
          Content(
            mediaType = "plain/text",
            schema = Schema(implementation = String::class),
          ),
        ],
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
        responseCode = "404",
        description = "Not Found, configured template file not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "500",
        description = "Unexpected error occurred",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getServiceTemplate(): ResponseEntity<Any> = if (subjectAccessRequestTemplatePath.isBlank()) {
    LOG.error("subject-access-request.template-path configuration value is blank")

    ResponseEntity
      .internalServerError()
      .headers(HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON })
      .body(
        ErrorResponse(
          status = HttpStatus.INTERNAL_SERVER_ERROR,
          userMessage = "A subject access request mustache template has not been configured for this service.",
          developerMessage = "A subject access request mustache template has not been configured for this service.",
        ),
      )
  } else {
    this::class.java.getResourceAsStream(subjectAccessRequestTemplatePath)
      ?.bufferedReader(Charsets.UTF_8)
      ?.use {
        ResponseEntity(
          it.readText(),
          HttpHeaders().apply { contentType = MediaType.TEXT_PLAIN },
          HttpStatus.OK,
        )
      }
      ?: run {
        LOG.error(
          "subject-access-request.template-path: '{}' file not found",
          ClassPathResource(subjectAccessRequestTemplatePath).path,
        )

        ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .headers(HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON })
          .body(
            ErrorResponse(
              status = HttpStatus.NOT_FOUND,
              userMessage = "Configured subject access request mustache template not found",
              developerMessage = "Configured subject access request mustache template not found",
            ),
          )
      }
  }

  protected fun sarTemplateConfigurationMissingException() = IllegalStateException(
    "Mandatory configuration blank/missing: HMPPS services implementing the " +
      "${HmppsSubjectAccessRequestReactiveService::class.simpleName} interface MUST provide a configuration value for " +
      "'subject-access-request.template-path'",
  )

  protected fun sarTemplateConfigurationInvalidException() = IllegalStateException(
    "Invalid subject access request configuration. Configured subject access request template file: " +
      "'subject-access-request.template-path': ${ClassPathResource(subjectAccessRequestTemplatePath).path} not found",
  )
}
