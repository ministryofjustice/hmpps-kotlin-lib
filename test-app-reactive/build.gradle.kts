plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.13.0"
  kotlin("plugin.spring") version "1.9.22"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
  implementation { exclude(module = "spring-boot-starter-web") }
  implementation { exclude(module = "spring-boot-starter-tomcat") }
}

dependencies {
  implementation(project(":hmpps-kotlin-spring-boot-starter"))
  implementation("org.springframework.boot:spring-boot-starter-webflux")

  testImplementation("org.wiremock:wiremock-standalone:3.3.1")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.3")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
}