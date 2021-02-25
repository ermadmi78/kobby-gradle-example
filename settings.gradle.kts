@file:Suppress("UnstableApiUsage")

rootProject.name = "kobby-gradle-example"

pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    }
}

include("cinema-api")
