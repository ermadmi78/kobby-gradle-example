import io.github.ermadmi78.kobby.kobby

description = "Cinema API Example"

plugins {
    kotlin("jvm")
    `java-library`
    id("io.github.ermadmi78.kobby") version "1.0.0-beta.02"
}

kobby {
    kotlin {
        scalars = mapOf(
            "Date" to typeOf("java.time", "LocalDate"),
            "JSON" to typeMap.parameterize(typeString, typeAny.nullable())
        )
    }
}

val kotlinJvmVersion: String by project
tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = kotlinJvmVersion
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
}

val jacksonVersion: String by project
dependencies {
    compileOnly("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
}