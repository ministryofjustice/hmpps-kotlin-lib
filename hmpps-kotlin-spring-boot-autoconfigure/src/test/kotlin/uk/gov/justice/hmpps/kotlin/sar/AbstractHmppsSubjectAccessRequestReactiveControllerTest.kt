package uk.gov.justice.hmpps.kotlin.sar

import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

abstract class AbstractHmppsSubjectAccessRequestReactiveControllerTest {

  protected val TEMPLATE_BLANK_ERROR_MESSAGE =
    "A subject access request mustache template has not been configured for this service."

  protected val TEMPLATE_NOT_FOUND_ERROR_MESSAGE =
    "Configured subject access request mustache template not found"

  protected val TEST_TEMPLATE_PATH = "/sar/test-template.mustache"

  protected val EXPECTED_TEMPLATE_BODY = "Subject Access Request: Test Template"

  protected fun assertErrorResponse(
    response: ResponseEntity<Any>,
    expectedStatusCode: HttpStatusCode,
    expectedErrorMessage: String,
  ) {
    assertThat(response).isNotNull
    assertThat(response.statusCode).isEqualTo(expectedStatusCode)
    assertThat(response.body).isNotNull
    assertThat(response.body).isInstanceOf(ErrorResponse::class.java)

    val body = (response.body as ErrorResponse)
    assertThat(body).isNotNull
    assertThat(body.status).isEqualTo(expectedStatusCode.value())
    assertThat(body.userMessage).isEqualTo(expectedErrorMessage)
    assertThat(body.developerMessage).isEqualTo(expectedErrorMessage)
  }

  protected fun assertSuccessResponse(response: ResponseEntity<Any>) {
    assertThat(response).isNotNull
    assertThat(response.statusCode).isEqualTo(OK)
    assertThat(response.body).isNotNull
    assertThat(response.body).isInstanceOf(ByteArray::class.java)

    val actualTemplate = String((response.body as ByteArray))
    assertThat(actualTemplate).isEqualTo(EXPECTED_TEMPLATE_BODY)
  }
}
