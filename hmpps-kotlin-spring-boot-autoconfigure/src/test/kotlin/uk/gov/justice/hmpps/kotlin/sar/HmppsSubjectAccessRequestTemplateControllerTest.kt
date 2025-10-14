package uk.gov.justice.hmpps.kotlin.sar

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND

class HmppsSubjectAccessRequestTemplateControllerTest : AbstractHmppsSubjectAccessRequestTemplateControllerTest() {

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

  @Test
  fun `validate throw exception if path is empty`() {
    val controller = HmppsSubjectAccessRequestTemplateController(
      subjectAccessRequestTemplatePath = "",
    )
    val ex = assertThrows<IllegalStateException> { controller.validateTemplateConfiguration() }

    assertThat(ex.message).isEqualTo(
      "Mandatory configuration blank/missing: HMPPS services implementing the HmppsSubjectAccessRequestService interface" +
        " MUST provide a configuration value for 'hmpps.sar.template.path'",
    )
  }

  @Test
  fun `validate throw exception if template file does not exist`() {
    val controller = HmppsSubjectAccessRequestTemplateController(
      subjectAccessRequestTemplatePath = "fictitious-template.mustache",
    )
    val ex = assertThrows<IllegalStateException> { controller.validateTemplateConfiguration() }

    assertThat(ex.message).isEqualTo(
      "Invalid subject access request configuration. Configured subject access request template file: " +
        "'hmpps.sar.template.path=fictitious-template.mustache' not found",
    )
  }
}
