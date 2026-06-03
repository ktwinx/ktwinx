plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.2.0"
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    pom {
        name.set("ktwinx-distributed")
        description.set("Utilities for KTwinX distributed applications.")
    }
}

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