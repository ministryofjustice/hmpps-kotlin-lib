# Authentication Holders

## What is provided?

A `HmppsAuthenticationHolder` or `HmppsReactiveAuthenticationHolder` for reactive applications.

This provides helper methods to get the HMPPS Auth token information e.g. `clientId`.

Most of the methods will throw an exception if there is no authentication or if the authentication isn't from HMPPS
Auth.  `authenticationOrNull` is the only one that will return `null` instead.  This alternative version is useful if
the service has event listeners or batch jobs where there won't be an authentication present.

Note that when integrating existing applications this can happen in tests as they often create
`TestingAuthenticationToken` or `UsernamePasswordAuthenticationToken` instances instead.  See
[WithMockAuthUser](/readme-contents/TestHelpers.md) for how to create the correct token for tests instead.

Also note that there are two versions of the holder.  We use
```kotlin
@ConditionalOnWebApplication(type = SERVLET)
```
and
```kotlin
@ConditionalOnWebApplication(type = REACTIVE)
```
to determine the correct component to create.  This means that for repository tests (e.g. annotated with `@DataJpaTest`)
where there is no web application loaded, the holder won't be automatically registered. Adding:
```yaml
  spring:
    main:
      web-application-type: servlet
```
to `application.yaml` will then ensure that the component is then loaded.

## What can I customize?

No customisation is possible - the holder is designed to replace existing `AuthenticationFacade` or
`UserContextUtils` implementations in existing projects.

## How do I opt out?

No opt out is needed - the component is registered but it is up to the application if it is used.

If the component registration is not required then it can be manually excluded from the application
```kotlin
@SpringBootApplication(exclude = [HmppsAuthenticationHolder::class])
```
