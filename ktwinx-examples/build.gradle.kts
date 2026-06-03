plugins {
    kotlin("jvm")
}

group = "io.github.ktwinx"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ktwinx-core"))
    implementation(project(":ktwinx-wldt-plugin"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(23)
}

tasks.test {
    useJUnitPlatform()
}