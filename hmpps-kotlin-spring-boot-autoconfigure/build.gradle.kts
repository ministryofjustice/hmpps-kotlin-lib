import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "2.2.10"
  kotlin("plugin.spring") version "2.2.10"
  id("maven-publish")
  id("signing")
  id("com.adarshr.test-logger") version "4.0.0"
  id("com.github.ben-manes.versions") version "0.52.0"
  id("se.patrikerdes.use-latest-versions") version "0.2.19"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
  id("org.owasp.dependencycheck") version "12.1.3"
  id("org.springframework.boot") version "3.5.5"
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-actuator")

  implementation("org.springdoc:springdoc-openapi-starter-common:2.8.13")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")

  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.18.1")

  testImplementation("org.assertj:assertj-core:3.27.4")
  testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
  testImplementation("org.mockito:mockito-junit-jupiter:5.19.0")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.mockito.kotlin:mockito-kotlin:6.0.0")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
  testImplementation("org.jetbrains.kotlin:kotlin-reflect")
}

publishing {
  repositories {
    mavenLocal()
  }
  publications {
    create<MavenPublication>("autoconfigure") {
      from(components["java"])
      pom {
        name.set(base.archivesName)
        artifactId = base.archivesName.get()
        description.set("A helper library to share common patterns for projects based from hmpps-template-kotlin")
        url.set("https://github.com/ministryofjustice/hmpps-kotlin-lib")
        licenses {
          license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
          }
        }
        developers {
          developer {
            id.set("mikehalmamoj")
            name.set("Mike Halma")
            email.set("mike.halma1@justice.gov.uk")
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
  sign(publishing.publications["autoconfigure"])
}
java.sourceCompatibility = JavaVersion.VERSION_21

tasks.bootJar {
  enabled = false
}

tasks.jar {
  enabled = true
}

repositories {
  mavenLocal()
  mavenCentral()
}

java {
  withSourcesJar()
  withJavadocJar()
}

fun isNonStable(version: String): Boolean {
  val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isStable = stableKeyword || regex.matches(version)
  return isStable.not()
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }

  withType<Test> {
    useJUnitPlatform()
  }

  withType<DependencyUpdatesTask> {
    rejectVersionIf {
      isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
  }
}

project.getTasksByName("check", false).forEach {
  val prefix = if (it.path.contains(":")) {
    it.path.substringBeforeLast(":")
  } else {
    ""
  }
  it.dependsOn("$prefix:ktlintCheck")
}

dependencyCheck {
  failBuildOnCVSS = 5f
  suppressionFiles = listOf("dps-gradle-spring-boot-suppressions.xml")
  format = "ALL"
  analyzers.assemblyEnabled = false
}

ktlint {
  version.set("1.5.0")
}
