import org.gradle.internal.os.OperatingSystem
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(compose.materialIconsExtended)

            //implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
            //implementation("org.jetbrains.compose.material3:material3:1.6.0")


        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(compose.material3)

            //implementation("org.jetbrains.compose.material3:material3:1.6.0")

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
            implementation("org.json:json:20231013")
            implementation("org.nd4j:nd4j-native-platform:1.0.0-beta7")
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.thesis.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DeepTwin"
            packageVersion = "1.0.0"
        }
    }
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Assembles a fat JAR for the Compose Desktop application."

    archiveBaseName.set("composeApp-desktop-fat")
    archiveVersion.set("1.0.0")
    manifest {
        attributes["Main-Class"] = "org.thesis.project.MainKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val desktopMain = kotlin.targets.getByName("desktop") as KotlinJvmTarget
    from(desktopMain.compilations["main"].output)

    val runtimeClasspath = configurations.getByName("desktopRuntimeClasspath")
    from({
        runtimeClasspath.filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    dependsOn("desktopMainClasses")
}


val os = OperatingSystem.current()

val platformFolder = when {
    os.isWindows -> "nifti_visualize_windows"
    os.isLinux -> "nifti_visualize_linux"
    os.isMacOsX -> "nifti_visualize_macos"
    else -> throw GradleException("Unsupported OS: ${os.name}")
}

tasks.register<Copy>("copyExecutablesToJarDir") {
    from("${project.projectDir}/external/$platformFolder")
    into("$buildDir/compose/jars/external/$platformFolder")
}

afterEvaluate {
    listOf(
        "packageUberJarForCurrentOS",
        "packageReleaseUberJarForCurrentOS",
        "packageReleaseMsi",
        "packageReleaseDmg",
        "packageReleaseDeb"
    ).forEach { taskName ->
        tasks.findByName(taskName)?.dependsOn("copyExecutablesToJarDir")
    }
}

tasks.register("packageAll") {
    dependsOn(
        "packageReleaseDmg",
        "packageReleaseMsi",
        "packageReleaseDeb"
    )
}








