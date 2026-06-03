plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.2.0"
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    pom {
        name.set("ktwinx-core")
        description.set("Core module of the KTwinX framework for human digital twins")
    }
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