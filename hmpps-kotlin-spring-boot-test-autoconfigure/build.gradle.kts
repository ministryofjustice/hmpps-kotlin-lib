import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "2.0.10"
  kotlin("plugin.spring") version "2.0.10"
  id("maven-publish")
  id("signing")
  id("com.adarshr.test-logger") version "4.0.0"
  id("com.github.ben-manes.versions") version "0.51.0"
  id("se.patrikerdes.use-latest-versions") version "0.2.18"
  id("io.spring.dependency-management") version "1.1.6"
  id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
  id("org.owasp.dependencycheck") version "8.4.3"
  id("org.springframework.boot") version "3.3.2"
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-test")
  implementation("org.springframework.security:spring-security-test")
  implementation(project(":hmpps-kotlin-spring-boot-starter"))

  implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
  implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
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
  suppressionFiles = listOf("dps-gradle-spring-boot-suppressions.xml", "hmpps-kotlin-spring-boot-test-autoconfigure/test-suppressions.xml")
  format = "ALL"
  analyzers.assemblyEnabled = false
}
