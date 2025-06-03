# App Insights Client Tracking

## What is provided?

By including the library you get basic App Insights client tracking added to your application.
This includes:
* a basic Spring `HandlerInterceptor`/`WebFilter` to perform the client tracking (depending upon the Spring app being servlet or reactive based)
* `client_id` and `user_name` are extracted from JWTs and added to the App Insights `Properties` / `customDimensions` as `clientId` and `uername`

> [!WARNING]
> If you already have client tracking configured then this library tries to get out of your way and wil do nothing if you 
> have beans/classes named `ClientTrackingConfiguration`, `ClientTrackingInterceptor` or `ClientTrackingWebFilter`.
> This also means you cannot use these class names to configure this library because it will do nothing. 

## What can I customize?

You can declare your own `HmppsClientTrackingInterceptor` / `HmppsClientTrackingwebFilter` which has parameters to override:
* **includePaths** by default we track all paths, override with a list of custom include paths
* **excludePaths** by default we exclude zero paths, override with a list of custom exclude paths
* **setTrackingDetails** by default we track only `clientId` and `username` from the JWT, override to track your own key/value pairs

See the tests in subproject `test-app`/`test-app-reactive` package `...integration/clienttracking` for examples.

## How do I opt out?

Exclude the auto configuration in your application i.e.
```kotlin
@SpringBootApplication(exclude = [HmppsClientTrackingConfiguration::class])
```

## Real World Examples

Coming soon!