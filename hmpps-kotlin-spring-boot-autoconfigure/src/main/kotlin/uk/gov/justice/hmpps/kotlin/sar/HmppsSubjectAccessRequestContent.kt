package uk.gov.justice.hmpps.kotlin.sar

import io.swagger.v3.oas.annotations.media.Schema

data class HmppsSubjectAccessRequestContent(
  @field:Schema(description = "The content of the subject access request response")
  val content: Any,
  @field:Schema(description = "The details of any attachments for the subject access request response")
  val attachments: List<Attachment>? = null,
)

data class Attachment(
  @field:Schema(description = "The number of the attachment which will match any corresponding reference in the content section")
  val attachmentNumber: Int,
  @field:Schema(description = "The name or description of the attachment which will be included in the report")
  val name: String,
  @field:Schema(description = "The content type of the attachment")
  val contentType: String,
  @field:Schema(description = "The url to be used to download the attachment file")
  val url: String,
  @field:Schema(description = "The filename of attachment file")
  val filesize: Int,
  @field:Schema(description = "The size of the attachment file in bytes")
  val filename: String,
)
