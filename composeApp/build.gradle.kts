import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    kotlin("plugin.serialization")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)


}

kotlin {
    jvm("desktop")
    
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
            implementation("org.jetbrains.compose.material3:material3:1.7.0")


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
            packageName = "org.thesis.project"
            packageVersion = "1.0.0"
        }
    }
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("composeApp-desktop-fat")
    manifest {
        attributes["Main-Class"] = "org.thesis.project.MainKt"
    }

    val desktopTarget = kotlin.targets.getByName("desktop") as KotlinJvmTarget
    val compileTask = tasks.named(desktopTarget.compilations["main"].compileKotlinTaskName)
    dependsOn(compileTask)

    from(desktopTarget.compilations["main"].output)

    val runtimeClasspath = configurations.getByName("desktopRuntimeClasspath")
    dependsOn(runtimeClasspath)

    from({
        runtimeClasspath
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}
