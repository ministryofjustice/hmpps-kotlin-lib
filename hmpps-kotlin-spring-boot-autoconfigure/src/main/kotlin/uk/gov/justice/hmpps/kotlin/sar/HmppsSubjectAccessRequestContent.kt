package uk.gov.justice.hmpps.kotlin.sar

import io.swagger.v3.oas.annotations.media.Schema

data class HmppsSubjectAccessRequestContent(
  @Schema(description = "The content of the subject access request response")
  val content: Any,
)
