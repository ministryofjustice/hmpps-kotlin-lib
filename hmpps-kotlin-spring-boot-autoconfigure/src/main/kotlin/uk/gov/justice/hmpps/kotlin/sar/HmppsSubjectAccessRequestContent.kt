package uk.gov.justice.hmpps.kotlin.sar

import io.swagger.v3.oas.annotations.media.Schema

data class HmppsSubjectAccessRequestContent(
  @param:Schema(description = "The content of the subject access request response")
  val content: Any,
  val attachments: List<Attachment>? = null,
)

data class Attachment(
  val attachmentNumber: Int,
  val name: String,
  val contentType: String,
  val url: String,
  val filesize: Int,
  val filename: String,
)
