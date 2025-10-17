package uk.gov.justice.hmpps.kotlin.sar

import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

abstract class AbstractHmppsSubjectAccessRequestTemplateControllerTest {

  protected val unexpectedErrorMessage = "Unexpected error getting subject access request template"

  protected val testTemplatePath = "/sar/test-template.mustache"

  protected val expectedTemplateBody = "Subject Access Request: Test Template"

  protected fun assertErrorResponse(
    response: ResponseEntity<Any>,
    expectedStatusCode: HttpStatusCode,
    expectedErrorMessage: String,
  ) {
    assertThat(response).isNotNull
    assertThat(response.statusCode).isEqualTo(expectedStatusCode)
    assertThat(response.headers.contentType).isEqualTo(MediaType.APPLICATION_JSON)
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
    assertThat(response.headers.contentType).isEqualTo(MediaType.TEXT_PLAIN)
    assertThat(response.body).isNotNull
    assertThat(response.body).isInstanceOf(String::class.java)

    val actualTemplate = response.body as String
    assertThat(actualTemplate).isEqualTo(expectedTemplateBody)
  }
}
