package uk.gov.justice.hmpps.kotlin.sar

import java.time.LocalDate

interface HmppsSubjectAccessRequestService

/**
 * Interface to be implemented if service only handles Prisoner Reference Numbers aka Prison Number.
 */
interface HmppsPrisonSubjectAccessRequestService : HmppsSubjectAccessRequestService {
  fun getPrisonContentFor(prn: String, fromDate: LocalDate?, toDate: LocalDate?): HmppsSubjectAccessRequestContent?
}

/**
 * Interface to be implemented if service only handles Case Reference Numbers
 */
interface HmppsProbationSubjectAccessRequestService : HmppsSubjectAccessRequestService {
  fun getProbationContentFor(crn: String, fromDate: LocalDate?, toDate: LocalDate?): HmppsSubjectAccessRequestContent?
}

/**
 * Interface to be implemented if service handles both Prisoner Reference Numbers and Case Reference Numbers
 */
interface HmppsPrisonProbationSubjectAccessRequestService : HmppsSubjectAccessRequestService {
  fun getContentFor(prn: String?, crn: String?, fromDate: LocalDate?, toDate: LocalDate?): HmppsSubjectAccessRequestContent?
}
