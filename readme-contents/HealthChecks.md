# Health Checks

## What is provided?

By including the library you get 2 abstract classes to extend to create your own `/health/ping` checks against external APIs.

For servlet based web servers extend class `HealthPingCheck`. Examples of extending this class can be found in `test-app` file `HealthCheck.kt`.

For reactive based web servers extend class `ReactiveHealthPingCheck`. Examples of extending this class can be found in `test-app-reactive` file `HealthCheck.kt`.

## What can I customize?

There is no customization provided for any of the above, so you'd need to opt out entirely to change the default behaviour.

## How do I opt out?

Manually create your own health check beans and don't extend the provided abstract classes.

## Real World Examples

Search Github for real world examples:

[Servlet](https://github.com/search?q=org%3Aministryofjustice+uk.gov.justice.hmpps.kotlin.health.HealthPingCheck&type=code)

[Reactive](https://github.com/search?q=org%3Aministryofjustice+uk.gov.justice.hmpps.kotlin.health.ReactiveHealthPingCheck&type=code)