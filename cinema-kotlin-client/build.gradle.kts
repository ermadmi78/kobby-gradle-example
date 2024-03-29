import org.springframework.boot.gradle.plugin.SpringBootPlugin

description = "Cinema Kotlin Client Example"

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    `java-library`
    id("org.springframework.boot")
}

val kotlinJdkVersion: String by project
kotlin {
    jvmToolchain(kotlinJdkVersion.toInt())
}

val kotlinVersion: String by project
val serializationVersion: String by project
val kotlinxCoroutinesVersion: String by project
val ktorVersion: String by project

dependencies {
    implementation(project(":cinema-api"))

    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")

    implementation(kotlin("stdlib", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
}