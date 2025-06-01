import io.github.ermadmi78.kobby.kobby

description = "Cinema API Example"

buildscript {
    dependencies {
        classpath("io.github.ermadmi78:kobby-gradle-plugin:5.1.1-SNAPSHOT")
    }
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `java-library`
    //id("io.github.ermadmi78.kobby") version "5.0.0"
}

apply(plugin = "io.github.ermadmi78.kobby")

kobby {
    kotlin {
        scalars = mapOf(
            "Date" to typeOf("java.time", "LocalDate")
                .serializer(
                    "io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto",
                    "LocalDateSerializer"
                ),
            "JSON" to typeOf("kotlinx.serialization.json", "JsonObject")
        )

        entity {
            projection {
                enableNotationWithoutParentheses = true
            }
        }
    }
}

val kotlinJdkVersion: String by project
kotlin {
    jvmToolchain(kotlinJdkVersion.toInt())
}

val serializationVersion: String by project
val ktorVersion: String by project
dependencies {
    // Add this dependency to enable Kotlinx Serialization
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    // Add this dependency to enable default Ktor adapters generation
    compileOnly("io.ktor:ktor-client-cio:$ktorVersion")
}