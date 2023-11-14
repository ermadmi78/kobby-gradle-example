@file:Suppress("UnstableApiUsage")

rootProject.name = "kobby-gradle-example"

pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val flywaydbVersion: String by settings
    val testLogger: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("org.flywaydb.flyway") version flywaydbVersion
        id("com.adarshr.test-logger") version testLogger
    }
}

include("cinema-api")
include("cinema-server")
include("cinema-kotlin-client")
