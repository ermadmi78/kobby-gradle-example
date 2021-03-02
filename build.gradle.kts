description = "Kotlin DSL over GraphQL schema generator"

plugins {
    kotlin("jvm") apply false
}

allprojects {
    buildscript {
        repositories {
            mavenLocal()
            mavenCentral()
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
}

