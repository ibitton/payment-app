import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    kotlin("plugin.serialization") version "2.3.10"
}

kotlin {
    androidLibrary {
        namespace = "com.cashi.challenge.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    jvm()

    js {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            // Ktor Client for API calls
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.serializationKotlinxJson)
            implementation(libs.ktor.clientLogging)
            
            // Kotlinx Serialization
            implementation(libs.kotlinx.serialization.json)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // Kotlinx DateTime
            implementation(libs.kotlinx.datetime)
            
            // Firebase KMP
            implementation(libs.firebase.common)
            implementation(libs.firebase.firestore)
            
            // Koin DI
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            // Ktor Client Android engine
            implementation(libs.ktor.clientAndroid)
            // Firebase Android dependencies
            implementation(libs.firebase.firestore.ktx)
        }
        jvmTest.dependencies {
            // Ktor Mock Engine for testing
            implementation(libs.ktor.clientMock)
            // Kotlin Test with JUnit 5 (required for Spek)
            implementation(libs.kotlin.test.junit5)
            // Spek BDD Testing Framework
            implementation(libs.spek.dsl)
            implementation(libs.spek.runner)
        }
    }
}

// Configure JVM tests to use JUnit Platform (required for Spek)
tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        includeEngines("spek2")
    }
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}
