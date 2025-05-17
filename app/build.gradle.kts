plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.peh_goapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.peh_goapp"
        minSdk = 29
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    kapt {
        javacOptions {
            option("-Xmaxerrs", "500")
        }
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.03.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // AndroidX Core & Lifecycle
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // Encrypted Shared Preferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Hilt
    implementation(libs.hilt.android.v249)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose.v120)

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Coil - Image loading
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Untuk akses media
    implementation(libs.androidx.activity.compose.v190)
    implementation(libs.androidx.activity.ktx)

    // Untuk pengelolaan gambar
    implementation(libs.coil.kt.coil.compose)

    //testing
    testImplementation(libs.hilt.navigation.compose.v120)

    //camerax
    // Tambahkan dependensi untuk camera dan barcode scanning
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.barcode.scanning)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
}