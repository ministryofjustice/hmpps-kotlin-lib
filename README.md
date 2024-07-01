# hmpps-kotlin-lib

A Spring Boot Starter library to share common patterns for projects based from [hmpps-template-kotlin](https://github.com/ministryofjustice/hmpps-template-kotlin)

## Overview

Many undocumented patterns have emerged in projects based on the Kotlin template. This library attempts to capture some of those patterns to make them easily available to other projects.

The library is a Spring Boot Starter that provides opinionated default configurations for various components. If popular variations exist for a component then the library aims to provide easy to use customizations for that component. If you wish to override default configurations then the library should get out of your way.

## Usage

### Using the library in a new Kotlin template project

The library is already included in the Kotlin template project and you will inherit its default functionality. This should be enough to get your started.

Once your project hits the real world and you start bumping into harder problems you may wish to customize or opt out of some of the library's components. See the [Components](#components) section for details of what the library provides.

### Adding the library to your existing project

To include this library in your project add the following to your `build.gradle.kts`:

```kotlin
dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.1")
}
```

You should (hopefully) find that the library does nothing to your project as you should have overridden any beans or configurations that the library provides.

Where possible we recommend that you use the library's default configurations and customizations. This will generally involve removing beans and possibly customizing the library's default configuration if required. See the [Components](#components) section for details of what the library provides.

## Components
* [Spring Security Resource Server](readme-contents/SpringResourceServer.md)
* [Subject Access Request Endpoint](readme-contents/SubjectAccessRequestEndpoint.md)
* [Web Clients](readme-contents/WebClients.md)
* [Health Checks](readme-contents/HealthChecks.md)
* [Info Contributors](readme-contents/InfoContributors.md)

## Publishing Locally (to test against other projects)

* Bump the version of this project in `build.gradle.kts` e.g. increase the minor version by 1 and add `-beta` to the version number.
* Publish the plugin to local maven with command:

```
./gradlew publishToMavenLocal 

```

In the other project's Gradle build script change the version to match and it should now be pulled into the project.

## Publishing to Maven Central

The Circle build pipeline for the `main` branch has a step to manually approve a publish to Maven Central. If you do not have permission to approve this step please ask in Slack channel `#hmpps_dev` to find someone that does.

### Published Version Numbers

Please be aware that once the jar is published to Maven Central other teams can use that version. They may even upgrade to the new version automatically with some fancy tooling.

Use some common sense when changing the version number and publishing:
* Try NOT to introduce breaking changes. Be creative, there are often ways around this
* Use [semantic versioning](https://semver.org/) to indicate the scope of the change
* You might think you can only test your change in the wild - consider [testing locally on other projects](#publishing-locally-to-test-against-other-projects) first
* If you must test in the wild, add a suffix to the version number such as `-beta` or `-wip` to indicate the change is not considered stable

## Technical Details of Publishing to Maven Central

[This guide](https://central.sonatype.org/publish/publish-guide/) was used as a basis for publishing to Maven Central.

However, please note that the document above is old and a couple of things have changed.

* The Gradle plugin used in that document - `maven` - is out of date and we use the [maven-publish plugin](https://docs.gradle.org/current/userguide/publishing_maven.html) instead.
* The process described in the document above requires a manual step to release the library from the Nexus staging repository - we have implemented the  [Nexus Publish Plugin](https://github.com/gradle-nexus/publish-plugin) to automate this step.

### Authenticating with Sonatype

When publishing to Maven Central we authenticate with Sonatype via a username and password before publishing to their Nexus repository.

To get access to the domain `uk.org.justice.service.hmpps` on Sonatype Nexus:

* [Create a Sonatype user account](https://issues.sonatype.org/secure/Signup!default.jspa)
* Get an existing Sonatype user with access to the domain to [raise a ticket](https://issues.sonatype.org/secure/CreateIssue.jspa) requesting access for the new user account. Ask in `#hmpps_dev` to find an existing Sonatype user.

#### Handling a Failed Publish

Check the [Staging repository](https://s01.oss.sonatype.org/#stagingRepositories) which is used to validate Maven publications before they are published and should have some errors you can fix. 

#### Adding Credentials to a Publish Request

In `build.gradle.kts` we use environment variables `OSSRH_USERNAME` and `OSSRH_PASSWORD` to authenticate with Sonatype. These environment variables must be set when running the `publish` task.

Note that this means the environment variables have been [set in Circle CI](https://app.circleci.com/settings/project/github/ministryofjustice/hmpps-kotlin-lib/environment-variables). This is safe as environment variables cannot be retrieved from Circle.

#### Changing the Sonatype Credentials

If you need to change the secrets used to authorise with Sonatype delete the Circle CI environment variables (`OSSRH_USERNAME` and `OSSRH_PASSWORD`) and re-add them with the username and password of another Sonatype user with access to the domain.

### Signing a Publish Request to Maven Central

One of the requirements for publishing to Maven Central is that all publications are [signed using PGP](https://central.sonatype.org/publish/requirements/gpg/).

#### Signing a Publication on Circle CI

In `build.gradle.kts` we use environment variables `ORG_GRADLE_PROJECT_signingKey` and `ORG_GRADLE_PROJECT_signingPassword` as recommended in the [Gradle Signing Plugin documentation](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:in-memory-keys).

#### Changing the Signing Key

* Generate a new key - follow the [Sonatype guide](https://central.sonatype.org/publish/requirements/gpg/). 
* Store the passphrase as you'll need it later.
* Remember to [distribute the public key](https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key) to a key server.
* Export the private key to a file - google for `gpg export private key` and you should find several guides for using `gpg --export-secret-keys`.
* To allow the private key to be inserted into a Circle env var make sure newlines in the private key are replaced with text `\n` so the key fits on a single line.
* Delete the environment variables `ORG_GRADLE_PROJECT_signingKey` and `ORG_GRADLE_PROJECT_signingPassword` from the [Circle CI env vars page](https://app.circleci.com/settings/project/github/ministryofjustice/hmpps-kotlin-lib/environment-variables)
* Recreate the environment variables where `ORG_GRADLE_PROJECT_signingKey` contains the private key (on a single line) and `ORG_GRADLE_PROJECT_signingPassword` contains the key's passphrase.
