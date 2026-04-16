import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.dokka") version "2.2.0"
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mm.astraisandroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mm.astraisandroid"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        val googleWebClientId = properties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: "\"\""

        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", googleWebClientId)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ### Compose ###
    // BOM
    implementation(platform("androidx.compose:compose-bom:2026.02.00"))
    // Biblioteca
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")

    // ### Navigation ###
    implementation("androidx.navigation:navigation-compose:2.8.9")

    // ### Ktor ###
    val ktorVers = "3.1.3"
    implementation("io.ktor:ktor-client-android:$ktorVers")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVers")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVers")
    implementation("io.ktor:ktor-client-logging:$ktorVers")
    implementation("io.ktor:ktor-client-auth:$ktorVers")

    // ## Corrutina ##
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // ### JWT ###
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // ### Lottie ###
    implementation("com.airbnb.android:lottie-compose:6.4.0")

    // ### ROOM ###
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ### SEGURIDAD ###
    implementation("androidx.security:security-crypto:1.1.0")

    // ### Hilt ###
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ### Google Credentials ###
    implementation("androidx.credentials:credentials:1.2.1")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.1")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
}