plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.8"
  kotlin("plugin.spring") version "2.0.21"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation(project(":hmpps-kotlin-spring-boot-starter"))
  implementation("org.springframework.boot:spring-boot-starter-webflux")

  testImplementation(project(":hmpps-kotlin-spring-boot-starter-test"))
  testImplementation("org.wiremock:wiremock-standalone:3.9.2")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}
