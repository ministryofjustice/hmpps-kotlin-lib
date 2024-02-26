# Web Client Configuration

## What is provided?

For servlet based web servers:
* an `OAuth2AuthorizedClientManager` bean is created
* an extension function to `WebClient.Builder` called `authorisedWebClient` for creating `WebClient`s that are authorized with an OAuth2 token
* an extension function to `WebClient.Builder` called `healthWebClient` for creating `WebClient`s that are unauthorized and are used to call `/health` endpoints

For an example of how to create `WebClient` instances see class `WebClientConfiguration` in subproject `test-app`.

For reactive based web servers:
* a `ReactiveOAuth2AuthorizedClientManager` bean is created
* an extension function to `WebClient.Builder` called `reactiveAuthorisedWebClient` for creating `WebClient`s that are authorized with an OAuth2 token
* an extension function to `WebClient.Builder` called `reactiveHealthWebClient` for creating `WebClient`s that are unauthorized and are used to call /health endpoints

For an example of how to create `WebClient` instances see class `WebClientConfiguration` in subproject `test-app-reactive`

## What can I customize?

There is no customization provided for any of the above, so you'd need to opt out entirely to change the default behaviour.

## How do I opt out?

The `OAuth2AuthorizedClientManager`/`ReactiveOAuth2AuthorizedClientManager` beans have annotation `@ConditionalOnMissingBean` so you can override them with your own implementation.

Though we recommend using the `WebClient.Builder` extension functions to create `WebClient` instances, you can still inject a `WebClient.Builder` into `@Configuration` to create your own customized `WebClient`s.
