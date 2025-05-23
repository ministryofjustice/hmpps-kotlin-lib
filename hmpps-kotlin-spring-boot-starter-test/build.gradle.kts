import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
  kotlin("jvm") version "2.1.21"
  id("maven-publish")
  id("signing")
  id("com.github.ben-manes.versions") version "0.52.0"
  id("se.patrikerdes.use-latest-versions") version "0.2.18"
}

dependencies {
  api(project(":hmpps-kotlin-spring-boot-test-autoconfigure"))
  api(platform("org.springframework.boot:spring-boot-dependencies:3.5.0"))
  api("org.springframework.boot:spring-boot-starter-test")

  api("io.jsonwebtoken:jjwt-impl:0.12.6")
  api("io.jsonwebtoken:jjwt-jackson:0.12.6")
}

publishing {
  repositories {
    mavenLocal()
  }
  publications {
    create<MavenPublication>("starter") {
      from(components["java"])
      pom {
        name.set(base.archivesName)
        artifactId = base.archivesName.get()
        description.set("A test helper library to share common patterns for projects based from hmpps-template-kotlin")
        url.set("https://github.com/ministryofjustice/hmpps-kotlin-lib")
        licenses {
          license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
          }
        }
        developers {
          developer {
            id.set("petergphillips")
            name.set("Peter Phillips")
            email.set("peter.phillips@digital.justice.gov.uk")
          }
        }
        scm {
          url.set("https://github.com/ministryofjustice/hmpps-kotlin-lib")
        }
      }
    }
  }
}

tasks.withType<PublishToMavenLocal> {
  signing {
    setRequired { false }
  }
}

signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications["starter"])
}
java.sourceCompatibility = JavaVersion.VERSION_21

kotlin {
  jvmToolchain(21)
}

repositories {
  mavenLocal()
  mavenCentral()
}

fun isNonStable(version: String): Boolean {
  val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isStable = stableKeyword || regex.matches(version)
  return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
  rejectVersionIf {
    isNonStable(candidate.version) && !isNonStable(currentVersion)
  }
}
