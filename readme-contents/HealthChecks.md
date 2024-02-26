# Health Checks

## What is provided?

By including the library you get 2 abstract classes to extend to create your own health checks against external APIs.

For servlet based web servers extend class `HealthCheck`. Examples of extending this class can be found in `test-app` file `HealthCheck.kt`.

For reactive based web servers extend class `ReactiveHealthCheck`. Examples of extending this class can be found in `test-app-reactive` file `HealthCheck.kt`.

## What can I customize?

There is no customization provided for any of the above, so you'd need to opt out entirely to change the default behaviour.

## How do I opt out?

Manually create your own health check beans and not extend the provided abstract classes.
