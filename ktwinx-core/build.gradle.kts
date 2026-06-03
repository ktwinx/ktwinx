plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.2.0"
    signing
    id("com.vanniktech.maven.publish")
}

java {
    withJavadocJar()
    withSourcesJar()
}

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