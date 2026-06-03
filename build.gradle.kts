plugins {
    kotlin("jvm") version "2.3.10"
    signing
    id("com.vanniktech.maven.publish") version "0.36.0"
}

val projectVersion: String = rootProject.file("version.txt").readText().trim()

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

allprojects {
    group = "io.github.ktwinx"
    version = projectVersion
}

subprojects {
    // Optional: shared dependency versions
    val kotestVersion = "5.8.0"
    val logbackVersion = "1.4.14"

    plugins.withId("org.jetbrains.kotlin.jvm") {
        dependencies {
            "implementation"("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            "implementation"("ch.qos.logback:logback-classic:$logbackVersion")
            "testImplementation"("io.kotest:kotest-runner-junit5:$kotestVersion")
            "testImplementation"("io.kotest:kotest-assertions-core:$kotestVersion")
            "testImplementation"("io.kotest:kotest-framework-engine:$kotestVersion")
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}

tasks.test {
    useJUnitPlatform()
}