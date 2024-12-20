plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.1.1"
  kotlin("plugin.spring") version "2.0.21"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
  implementation { exclude(module = "spring-boot-starter-web") }
  implementation { exclude(module = "spring-boot-starter-tomcat") }
}

dependencies {
  implementation(project(":hmpps-kotlin-spring-boot-starter"))
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  testImplementation(project(":hmpps-kotlin-spring-boot-starter-test"))
  testImplementation("org.wiremock:wiremock-standalone:3.10.0")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}
