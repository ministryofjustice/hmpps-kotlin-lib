package uk.gov.justice.hmpps.kotlin.sar

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND

class HmppsSubjectAccessRequestReactiveControllerTest : AbstractHmppsSubjectAccessRequestReactiveControllerTest() {
  private val subjectAccessRequestReactiveService: HmppsSubjectAccessRequestReactiveService = mock()

  @Nested
  inner class GetSubjectAccessRequestTemplate {

    @Test
    fun `should return status 500 when template is blank`() {
      val controller = HmppsSubjectAccessRequestReactiveController(
        service = subjectAccessRequestReactiveService,
        subjectAccessRequestTemplatePath = "",
      )

      assertErrorResponse(
        response = controller.getServiceTemplate(),
        expectedStatusCode = INTERNAL_SERVER_ERROR,
        expectedErrorMessage = TEMPLATE_BLANK_ERROR_MESSAGE,
      )
    }

    @Test
    fun `should return status 404 when configured template does not exist`() {
      val controller = HmppsSubjectAccessRequestReactiveController(
        service = subjectAccessRequestReactiveService,
        subjectAccessRequestTemplatePath = "fictitious-template.mustache",
      )

      assertErrorResponse(
        response = controller.getServiceTemplate(),
        expectedStatusCode = NOT_FOUND,
        expectedErrorMessage = TEMPLATE_NOT_FOUND_ERROR_MESSAGE,
      )
    }

    @Test
    fun `should return expected template content`() {
      val controller = HmppsSubjectAccessRequestReactiveController(
        service = subjectAccessRequestReactiveService,
        subjectAccessRequestTemplatePath = TEST_TEMPLATE_PATH,
      )

      assertSuccessResponse(response = controller.getServiceTemplate())
    }
  }
}