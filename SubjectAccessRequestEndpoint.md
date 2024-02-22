# Subject Access Request Endpoint

## What is provided?

If you implement one of the three subject access request interfaces then a `/subject-access-request` endpoint will
be automatically created and call your service.  The endpoint will be protected by a `SAR_DATA_ACCESS` role.

If you do not implement the service then no endpoint will be created.

## What can I customize?

There are three options:

1. For applications that *only* store prisoner information implement `HmppsPrisonSubjectAccessRequestService`.
If the subject access request endpoint is called with a prison number then your service will be called. 
1. For applications that *only* store probation information implement `HmppsProbationSubjectAccessRequestService`.
If the subject access request endpoint is called with a case reference number then your service will be called.
1. For applications that store prisoner and also probation information then implement `HmppsPrisonProbationSubjectAccessRequestService`.
If either a prison number or case reference number is supplied to the endpoint then your service will be called.

## How do I opt out?

If you do not implement the service interface then no endpoint will be created.

## Testing your service
See the `SubjectAccessRequestServiceSampleTest` and `SubjectAccessRequestSampleIntegrationTest` tests in the subproject
`test-app` package for sample tests to ensure your service and endpoint is working as expected.
