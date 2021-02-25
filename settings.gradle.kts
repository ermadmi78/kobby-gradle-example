@file:Suppress("UnstableApiUsage")

rootProject.name = "kobby-gradle-example"

pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
    }
}

include("cinema-api")
