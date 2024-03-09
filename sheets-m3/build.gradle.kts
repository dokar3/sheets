import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvmToolchain(11)

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "sheets-m3"
        browser {
            commonWebpackConfig {
                outputFileName = "sheets-m3.js"
            }
        }
        binaries.executable()
    }

    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            api(project(":sheets-core"))
            api(compose.material3)
        }
        desktopMain.dependencies {
            api(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = "com.dokar.sheets.m3"
    compileSdk = rootProject.extra["compile_sdk"] as Int

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

compose.experimental {
    web.application {}
}