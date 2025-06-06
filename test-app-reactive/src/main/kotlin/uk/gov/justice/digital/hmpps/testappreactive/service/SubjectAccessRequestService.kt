package uk.gov.justice.digital.hmpps.testappreactive.service

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestReactiveService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Service
class SubjectAccessRequestService : HmppsPrisonSubjectAccessRequestReactiveService {
  override suspend fun getPrisonContentFor(prn: String, fromDate: LocalDate?, toDate: LocalDate?) = prn.takeIf { prn.startsWith("A") }?.let {
    HmppsSubjectAccessRequestContent(
      content = TestContent(
        prisonerNumber = prn,
        commentText = "some useful comment",
      ),
    )
  }
}

data class TestContent(
  val prisonerNumber: String,
  val commentText: String,
)
