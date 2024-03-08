package uk.gov.justice.hmpps.kotlin.sar

import java.time.LocalDate

interface HmppsSubjectAccessRequestReactiveService

/**
 * Interface to be implemented if service only handles Prisoner Reference Numbers aka Prison Number.
 */
interface HmppsPrisonSubjectAccessRequestReactiveService : HmppsSubjectAccessRequestReactiveService {
  suspend fun getPrisonContentFor(prn: String, fromDate: LocalDate?, toDate: LocalDate?): HmppsSubjectAccessRequestContent?
}

/**
 * Interface to be implemented if service only handles Case Reference Numbers
 */
interface HmppsProbationSubjectAccessRequestReactiveService : HmppsSubjectAccessRequestReactiveService {
  suspend fun getProbationContentFor(crn: String, fromDate: LocalDate?, toDate: LocalDate?): HmppsSubjectAccessRequestContent?
}

/**
 * Interface to be implemented if service handles both Prisoner Reference Numbers and Case Reference Numbers
 */
interface HmppsPrisonProbationSubjectAccessRequestReactiveService : HmppsSubjectAccessRequestReactiveService {
  suspend fun getContentFor(prn: String?, crn: String?, fromDate: LocalDate?, toDate: LocalDate?): HmppsSubjectAccessRequestContent?
}
