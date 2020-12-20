@file:Suppress("UnstableApiUsage")

rootProject.name = "kobby-gradle-integration-tests"

pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
    }
}

include("lib")
