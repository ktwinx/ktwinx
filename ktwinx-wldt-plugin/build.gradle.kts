plugins {
    kotlin("jvm")
    `maven-publish`
}

val moduleVersion: String = rootProject.file("${project.name}/version.txt").readText().trim()

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ktwinx-core"))
    implementation(project(":ktwinx-distributed"))
    implementation("io.github.wldt:wldt-core:0.4.0")
    implementation("io.github.wldt:mqtt-physical-adapter:0.1.2")
    implementation("io.github.wldt:mqtt-digital-adapter:0.1.2")
    implementation("io.github.wldt:http-digital-adapter:0.2")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(23)
}

tasks.test {
    useJUnitPlatform()
}