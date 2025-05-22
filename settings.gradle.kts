pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    plugins {
        id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
        id("org.jetbrains.kotlin.multiplatform") version "2.1.0"
        id("org.jetbrains.compose") version "1.8.1"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

}
include(":composeApp")

