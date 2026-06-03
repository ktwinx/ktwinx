plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "ktwinx"
include("ktwinx-core")
include("ktwinx-distributed")
include("ktwinx-wldt-plugin")
include("ktwinx-examples")