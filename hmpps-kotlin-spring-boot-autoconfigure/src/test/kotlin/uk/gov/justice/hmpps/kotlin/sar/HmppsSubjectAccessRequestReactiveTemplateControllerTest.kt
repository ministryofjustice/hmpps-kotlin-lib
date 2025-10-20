package uk.gov.justice.hmpps.kotlin.sar

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

class HmppsSubjectAccessRequestReactiveTemplateControllerTest : AbstractHmppsSubjectAccessRequestTemplateControllerTest() {

  @Test
  fun `should return status 500 when template is blank`() = runTest {
    val controller = HmppsSubjectAccessRequestReactiveTemplateController(
      templatePath = "",
    )

    assertErrorResponse(
      response = controller.getServiceTemplate(),
      expectedStatusCode = INTERNAL_SERVER_ERROR,
      expectedErrorMessage = unexpectedErrorMessage,
    )
  }

  @Test
  fun `should return status 500 when configured template does not exist`() = runTest {
    val controller = HmppsSubjectAccessRequestReactiveTemplateController(
      templatePath = "fictitious-template.mustache",
    )

    assertErrorResponse(
      response = controller.getServiceTemplate(),
      expectedStatusCode = INTERNAL_SERVER_ERROR,
      expectedErrorMessage = unexpectedErrorMessage,
    )
  }

  @Test
  fun `should return expected template content`() = runTest {
    val controller = HmppsSubjectAccessRequestReactiveTemplateController(
      templatePath = testTemplatePath,
    )

    assertSuccessResponse(response = controller.getServiceTemplate())
  }

  @Test
  fun `validate throws exception if path is empty`() {
    val controller = HmppsSubjectAccessRequestReactiveTemplateController(
      templatePath = "",
    )
    val ex = assertThrows<IllegalStateException> { controller.validateTemplateConfiguration() }

    assertThat(ex.message).isEqualTo(
      "Mandatory configuration blank/missing: HMPPS services implementing the HmppsSubjectAccessRequestReactiveService " +
        "interface MUST provide a configuration value for 'hmpps.sar.template.path'",
    )
  }

  @Test
  fun `validate throws exception if template file does not exist`() {
    val controller = HmppsSubjectAccessRequestReactiveTemplateController(
      templatePath = "fictitious-template.mustache",
    )
    val ex = assertThrows<IllegalStateException> { controller.validateTemplateConfiguration() }

    assertThat(ex.message).isEqualTo(
      "Invalid subject access request configuration. Configured subject access request template file: " +
        "'hmpps.sar.template.path=fictitious-template.mustache' not found",
    )
  }
}
