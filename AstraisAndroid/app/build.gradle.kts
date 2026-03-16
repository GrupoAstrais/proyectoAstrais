plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
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

    // ## Corrutina ##
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}