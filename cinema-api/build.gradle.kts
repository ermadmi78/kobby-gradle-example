import io.github.ermadmi78.kobby.kobby

description = "Cinema API Example"

plugins {
    kotlin("jvm")
    `java-library`
    id("io.github.ermadmi78.kobby") version "4.0.3"
}

kobby {
    kotlin {
        scalars = mapOf(
            "Date" to typeOf("java.time", "LocalDate"),
            "JSON" to typeMap.parameterize(typeString, typeAny.nullable())
        )
    }
}

val kotlinJdkVersion: String by project
kotlin {
    jvmToolchain(kotlinJdkVersion.toInt())
}

val jacksonVersion: String by project
val ktorVersion: String by project
dependencies {
    // Add this dependency to enable Jackson annotation generation in DTO classes by Kobby
    compileOnly("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")

    // Add this dependency to enable default Ktor adapters generation
    compileOnly("io.ktor:ktor-client-cio:$ktorVersion")
}