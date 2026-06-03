plugins {
    kotlin("jvm") version "2.3.10"
    id("com.vanniktech.maven.publish") version "0.36.0" apply false
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

    plugins.withId("com.vanniktech.maven.publish") {
        extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            publishToMavenCentral()
            signAllPublications()

            coordinates(
                groupId = "io.github.ktwinx",
                artifactId = project.name,
                version = projectVersion
            )

            pom {
                url.set("https://github.com/ktwinx/ktwinx")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("lm98")
                        name.set("Micelli Leonardo")
                        email.set("leonardomicelli@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/ktwinx/ktwinx.git")
                    developerConnection.set("scm:git:ssh://github.com/ktwinx/ktwinx.git")
                    url.set("https://github.com/ktwinx/ktwinx")
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}