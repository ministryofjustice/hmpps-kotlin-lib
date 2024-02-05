package uk.gov.justice.digital.hmpps.testappreactive

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TestAppReactive

fun main(args: Array<String>) {
  runApplication<TestAppReactive>(*args)
}
