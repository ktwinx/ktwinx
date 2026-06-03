plugins {
    kotlin("jvm")
    `maven-publish`
    kotlin("plugin.serialization") version "2.2.0"
}

val moduleVersion: String = rootProject.file("${project.name}/version.txt").readText().trim()


repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ktwinx-core"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(23)
}

tasks.test {
    useJUnitPlatform()
}