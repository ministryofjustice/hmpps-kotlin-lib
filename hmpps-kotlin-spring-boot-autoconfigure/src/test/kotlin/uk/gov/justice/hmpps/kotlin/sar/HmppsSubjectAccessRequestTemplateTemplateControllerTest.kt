package uk.gov.justice.hmpps.kotlin.sar

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND

class HmppsSubjectAccessRequestTemplateTemplateControllerTest :
  AbstractHmppsSubjectAccessRequestTemplateControllerTest() {

  @Test
  fun `should return status 500 when template is blank`() {
    val controller = HmppsSubjectAccessRequestTemplateController(
      subjectAccessRequestTemplatePath = "",
    )

    assertErrorResponse(
      response = controller.getServiceTemplate(),
      expectedStatusCode = INTERNAL_SERVER_ERROR,
      expectedErrorMessage = templateBlankErrorMessage,
    )
  }

  @Test
  fun `should return status 404 when configured template does not exist`() {
    val controller = HmppsSubjectAccessRequestTemplateController(
      subjectAccessRequestTemplatePath = "fictitious-template.mustache",
    )

    assertErrorResponse(
      response = controller.getServiceTemplate(),
      expectedStatusCode = NOT_FOUND,
      expectedErrorMessage = templateNotFoundErrorMessage,
    )
  }

  @Test
  fun `should return expected template content`() {
    val controller = HmppsSubjectAccessRequestTemplateController(
      subjectAccessRequestTemplatePath = testTemplatePath,
    )

    assertSuccessResponse(response = controller.getServiceTemplate())
  }
}
