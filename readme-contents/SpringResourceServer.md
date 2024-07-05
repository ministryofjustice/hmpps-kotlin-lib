# Spring Security Resource Server

## What is provided?

By including the library you get a basic Spring `SecurityFilterChain`/`SecurityWebFilterChain` bean configured. This includes:
* requiring a valid JWT token for any endpoint
* leaving common endpoints unauthorized (e.g. `/health`, `/info`, `/v3/api-docs`, `/swagger-ui/**`)
* providing an `AuthAwareTokenConverter`
* providing a cached `JwtDecoder`
* adding required spring security dependencies to your project, e.g. `spring-boot-starter-security`,
`spring-boot-starter-oauth2-client` and `spring-boot-starter-oauth2-resource-server`.

For full details of what is provided by default see subproject `hmpps-kotlin-spring-boot-autoconfigure` package `auth` class `HmppsResourceServerConfiguration`.

## What can I customize?

There are various customizations available. These include:
* adding other unauthorized paths
* setting a default role for authorized endpoints
* overriding the `authorizeHttpRequests` configuration entirely

For full details of what is customizable start in subproject `hmpps-kotlin-spring-boot-autoconfigure` package `auth` with file `ResourceServerConfigurationCustomizerDsl.kt`.

Look at the interfaces found in the various `*Dsl.kt` files to see what the customization DSL provides including examples in the Javadocs.

Also check the tests in subproject `test-app`/`test-app-reactive` package `...integration/auth/customizer` for working examples.

## How do I opt out?

To opt out of the library's resource server configuration entirely you can just create your own `SecurityFilterChain`/`SecurityWebFilterChain` bean.

Alternatively you can exclude the auto configuration in your application i.e.
```kotlin
@SpringBootApplication(exclude = [HmppsResourceServerConfiguration::class])
```

See the tests in the subproject `test-app`/`test-app-reactive` package `...integration/auth/overrides` for working examples.
