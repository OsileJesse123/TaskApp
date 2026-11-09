import org.gradle.api.tasks.Sync

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class CopyAppFunctionAssets : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun copyAssets() {
        val source = sourceDirectory.get().asFile
        val destination = outputDirectory.get().asFile

        destination.deleteRecursively()
        source.copyRecursively(destination, overwrite = true)
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.jesse.taskapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.jesse.taskapp"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.jesse.taskapp.TaskTestRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    androidComponents {
        onVariants { variant ->

            val variantName = variant.name.replaceFirstChar {
                it.uppercase()
            }

            val copyTask = tasks.register<CopyAppFunctionAssets>(
                "copy${variantName}AppFunctionAssets"
            ) {
                sourceDirectory.set(
                    project(":core").layout.buildDirectory.dir(
                        "generated/ksp/${variant.name}/resources/assets"
                    )
                )
            }

            copyTask.configure {
                dependsOn(":core:ksp${variantName}Kotlin")
            }

            variant.sources.assets?.addGeneratedSourceDirectory(
                copyTask,
                CopyAppFunctionAssets::outputDirectory
            )
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(project(":core"))
    implementation(project(":feature:tasklist"))
    implementation(project(":feature:taskdetail"))
    implementation(project(":feature:taskform"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.appfunctions)
    ksp(libs.hilt.compiler)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.room.runtime)
    kspAndroidTest(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}