package uk.gov.justice.hmpps.kotlin.sar

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

class HmppsSubjectAccessRequestTemplateControllerTest : AbstractHmppsSubjectAccessRequestTemplateControllerTest() {

  @Test
  fun `should return status 500 when template is blank`() {
    val controller = HmppsSubjectAccessRequestTemplateController(
      templatePath = "",
    )

    assertErrorResponse(
      response = controller.getServiceTemplate(),
      expectedStatusCode = INTERNAL_SERVER_ERROR,
      expectedErrorMessage = unexpectedErrorMessage,
    )
  }

  @Test
  fun `should return status 500 when configured template does not exist`() {
    val controller = HmppsSubjectAccessRequestTemplateController(
      templatePath = "fictitious-template.mustache",
    )

    assertErrorResponse(
      response = controller.getServiceTemplate(),
      expectedStatusCode = INTERNAL_SERVER_ERROR,
      expectedErrorMessage = unexpectedErrorMessage,
    )
  }

  @Test
  fun `should return expected template content`() {
    val controller = HmppsSubjectAccessRequestTemplateController(
      templatePath = testTemplatePath,
    )

    assertSuccessResponse(response = controller.getServiceTemplate())
  }

  @Test
  fun `validate throw exception if path is empty`() {
    val controller = HmppsSubjectAccessRequestTemplateController(
      templatePath = "",
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
      templatePath = "fictitious-template.mustache",
    )
    val ex = assertThrows<IllegalStateException> { controller.validateTemplateConfiguration() }

    assertThat(ex.message).isEqualTo(
      "Invalid subject access request configuration. Configured subject access request template file: " +
        "'hmpps.sar.template.path=fictitious-template.mustache' not found",
    )
  }
}
