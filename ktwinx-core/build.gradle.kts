plugins {
    kotlin("jvm")
    `maven-publish`
    kotlin("plugin.serialization") version "2.2.0"
    id("com.vanniktech.maven.publish")
}
val moduleVersion: String = rootProject.file("${project.name}/version.txt").readText().trim()


repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(23)
}

tasks.test {
    useJUnitPlatform()
}