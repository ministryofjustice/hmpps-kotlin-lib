package uk.gov.justice.digital.hmpps.testapp.service

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.kotlin.sar.Attachment
import uk.gov.justice.hmpps.kotlin.sar.AttachmentHeader
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Service
class SubjectAccessRequestService : HmppsPrisonSubjectAccessRequestService {
  override fun getPrisonContentFor(prn: String, fromDate: LocalDate?, toDate: LocalDate?) = prn.takeIf { prn.startsWith("A") }?.let {
    HmppsSubjectAccessRequestContent(
      content = TestContent(
        prisonerNumber = prn,
        commentText = "some useful comment",
      ),
      attachments = listOf(
        Attachment(
          attachmentNumber = 1,
          name = "Attachment Image",
          contentType = "image/gif",
          url = "http://url/image.gif",
          filesize = 1234,
          filename = "image.gif",
          headers = listOf(
            AttachmentHeader(
              name = "X-Header",
              value = "header-value",
            ),
          ),
        ),
      ),
    )
  }
}

data class TestContent(
  val prisonerNumber: String,
  val commentText: String,
)
