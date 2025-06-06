# Test Helpers

## What is provided?

1. `WithMockAuthUser` - an alternative version of `@WithMockUser` that will add an `AuthAwareAuthenticationToken`
to the current security context.
2. `JwtAuthorisationHelper` that provides a primary `JwtDecoder` bean for your tests so that HMPPS Auth isn't required
as a wiremock dependency.  It also provides helper methods to create JWT access tokens and set authorisation headers for
use in integration tests when calling with web clients.

## What can I customize?

`WithMockAuthUser` provides parameters to control the token created.  By default it will set
* a `username` of `user`
* role of `ROLE_USER`
* a `clientId` of `test-client-id`

## How do I opt out?

Since the `JwtAuthorisationHelper` automatically registers a primary `JwtDecoder` bean, it needs to be excluded from
any test configuration if it is not needed.  Note that this is an opt out - by default you will get the bean.

If multiple decoder beans are registered then this will mean that tests could fail with unauthorised errors as the JWT
in the context might not be decoded by the correct bean. 

## Real World Examples

Search Github for real world examples:

[WithMockAuthUser](https://github.com/search?q=org%3Aministryofjustice+uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser&type=code)

[JwtAuthenicationHelper](https://github.com/search?q=org%3Aministryofjustice+uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper&type=code)
