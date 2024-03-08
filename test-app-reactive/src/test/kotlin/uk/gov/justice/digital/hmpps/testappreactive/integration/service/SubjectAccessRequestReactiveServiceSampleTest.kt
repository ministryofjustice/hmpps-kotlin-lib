package uk.gov.justice.digital.hmpps.testappreactive.integration.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.testappreactive.service.SubjectAccessRequestService

class SubjectAccessRequestReactiveServiceSampleTest {
  private val service = SubjectAccessRequestService()

  @Test
  fun `returns null for not found`() {
    runBlocking {
      // service will return not found for prisoners that don't start with A
      assertThat(service.getPrisonContentFor("B12345", null, null)).isNull()
    }
  }

  @Test
  fun `returns data if prisoner found`() {
    runBlocking {
      // service will return data for prisoners that start with A
      val restrictedPatient = service.getPrisonContentFor("A12345", null, null)?.content

      assertThat(restrictedPatient).extracting("prisonerNumber").isEqualTo("A12345")
      assertThat(restrictedPatient).extracting("commentText").isEqualTo("some useful comment")
    }
  }
}
