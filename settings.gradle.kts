@file:Suppress("UnstableApiUsage")

rootProject.name = "kobby-gradle-example"

pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
    }
}

include("cinema-api")
include("cinema-server")
