package uk.gov.justice.digital.hmpps.testapp.integration.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.testapp.service.SubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.Attachment
import uk.gov.justice.hmpps.kotlin.sar.AttachmentHeader

class SubjectAccessRequestServiceSampleTest {
  private val service = SubjectAccessRequestService()

  @Test
  fun `returns null for not found`() {
    // service will return not found for prisoners that don't start with A
    assertThat(service.getPrisonContentFor("B12345", null, null)).isNull()
  }

  @Test
  fun `returns data if prisoner found`() {
    // service will return data for prisoners that start with A
    val restrictedPatient = service.getPrisonContentFor("A12345", null, null)?.content

    assertThat(restrictedPatient).extracting("prisonerNumber").isEqualTo("A12345")
    assertThat(restrictedPatient).extracting("commentText").isEqualTo("some useful comment")
  }

  @Test
  fun `returns attachment data if found`() {
    val attachments = service.getPrisonContentFor("A12345", null, null)?.attachments

    assertThat(attachments).singleElement().isEqualTo(
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
    )
  }
}
