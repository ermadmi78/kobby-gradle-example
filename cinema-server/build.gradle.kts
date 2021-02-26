import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "Cinema Server Example"

plugins {
    kotlin("jvm")
    `java-library`
}

val kotlinJvmVersion: String by project
val kotlinVersion: String by project
val kotestVersion: String by project
val jacksonVersion: String by project

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = kotlinJvmVersion
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    test {
        testLogging.showStandardStreams = true
        testLogging.exceptionFormat = FULL
        useJUnitPlatform()
    }
}

dependencies {
    implementation(project(":cinema-api"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    implementation(kotlin("stdlib", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
    testImplementation(kotlin("test", kotlinVersion))
    testImplementation(kotlin("test-junit5", kotlinVersion))

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
}