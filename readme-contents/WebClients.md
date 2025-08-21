# Web Client Configuration

## What is provided?

For servlet based web servers:
* a `ClientRegistrationRepository` bean is created
* a `OAuth2AuthorizedClientManager` bean is created
* a `OAuth2AuthorizedClientService` bean is created. This will be an instance of the `GlobalPrincipalOAuth2AuthorizedClientService` which
  caches client credentials tokens under a "global" principal name ("global-system-principal") instead of the name of the authenticate principal
  in the Spring `SecurityContextHolder`. This is to avoid unnecessary token requests to HMPPS Auth.
* an extension function to `WebClient.Builder` called `authorisedWebClient` for creating `WebClient`s that are authorized with an OAuth2 token
* an extension function to `WebClient.Builder` called `healthWebClient` for creating `WebClient`s that are unauthorized and are used to call `/health` endpoints
* a default timeout of 30 seconds when fetching client credentials 

For an example of how to create `WebClient` instances see class `WebClientConfiguration` in subproject `test-app`.

> **_NOTE:_**  The `GlobalPrincipalOAuth2AuthorizedClientService` should be the default `OAuth2AuthorizedClientService` unless is it being used with a `WebClient`
> which is configured to inject the name of the authenticated principal into the client credentials token request. In this scenario the tokens
> will be unique per user and should be cached under the authenticated principal. For this the `InMemoryOAuth2AuthorizedClientService` should be used instead.

For reactive based web servers:
* a `ClientRegistrationRepository` bean is created
* a `ReactiveOAuth2AuthorizedClientManager` bean is created
* a `ReactiveOAuth2AuthorizedClientService` bean is created. This will be an instance of the `ReactiveGlobalPrincipalOAuth2AuthorizedClientService` which
  caches client credentials tokens under a "global" principal name ("global-system-principal") instead of the name of the authenticate principal
  in the Spring `ReactiveSecurityContextHolder`. This is to avoid unnecessary token requests to HMPPS Auth.
* an extension function to `WebClient.Builder` called `reactiveAuthorisedWebClient` for creating `WebClient`s that are authorized with an OAuth2 token
* an extension function to `WebClient.Builder` called `reactiveHealthWebClient` for creating `WebClient`s that are unauthorized and are used to call /health endpoints
* a default timeout of 30 seconds when fetching client credentials

For an example of how to create `WebClient` instances see class `WebClientConfiguration` in subproject `test-app-reactive`

> **_NOTE:_**  The `ReactiveGlobalPrincipalOAuth2AuthorizedClientService` should be the default `ReactiveOAuth2AuthorizedClientService` unless is it being used with a `WebClient`
> which is configured to inject the name of the authenticated principal into the client credentials token request. In this scenario the tokens
> will be unique per user and should be cached under the authenticated principal. For this the `InMemoryReactiveOAuth2AuthorizedClientService` should be used instead.

## What can I customize?

It is possible to customize the default timeouts for the `WebClient` instances created. This would be done in the `@Bean` definition when creating web clients. For examples see class `WebClientConfiguration` in either `test-app` or `test-app-reavtive`.

You can change the default 30 second timeout when fetching client credentials. To do this provide your own `OAuth2AuthorizedClientProvider` / `ReactiveOAuth2AuthorizedClientProvider` bean using either the `WebClient.Builder.reactiveOAuth2AuthorizedClientProvider` or `oAuth2AuthorizedClientProvider` functions provided by the library. See examples in `WebClientCustomizerTest.CustomizerConfiguration` in `test-app` and `test-app-reactive`.

For additional customizations you would need to opt out entirely.

## How do I opt out?

The `OAuth2AuthorizedClientManager`/`ReactiveOAuth2AuthorizedClientManager` beans have annotation `@ConditionalOnMissingBean` so you can override them with your own implementation.

Though we recommend using the `WebClient.Builder` extension functions to create `WebClient` instances, you can still inject a `WebClient.Builder` into `@Configuration` to create your own customized `WebClient`s.

## Real World Examples

Search Github for real world examples:

[Servlet](https://github.com/search?q=org%3Aministryofjustice+uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient&type=code)

[Reactive](https://github.com/search?q=org%3Aministryofjustice+uk.gov.justice.hmpps.kotlin.auth.reactiveAuthorisedWebClient&type=code)