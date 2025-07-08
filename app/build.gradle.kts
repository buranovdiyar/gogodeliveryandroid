plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.gogomarket"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gogomarket"
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


    // Jetpack Compose Navigation + ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.navigation:navigation-compose:2.7.5")

// Kotlin Coroutines (если ещё нет)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.compose.material3:material3:1.2.0")
// OkHttp for API requests
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

// DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("io.coil-kt:coil-compose:2.5.0")
    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")

// Gson Converter
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.compose.material:material:1.6.8")
    // ML Kit (Barcode)
    implementation ("com.google.mlkit:barcode-scanning:17.2.0")
    implementation ("com.google.mlkit:barcode-scanning:17.1.0")
// CameraX
    implementation ("androidx.camera:camera-core:1.1.0")
    implementation ("androidx.camera:camera-camera2:1.1.0")
    implementation ("androidx.camera:camera-lifecycle:1.1.0")
    implementation  ("androidx.camera:camera-view:1.0.0-alpha32")
    implementation  ("androidx.camera:camera-extensions:1.0.0-alpha32")
    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")
    implementation ("androidx.camera:camera-extensions:1.3.0")

    implementation ("androidx.compose.material:material-icons-extended")

// Lifecycle
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation("androidx.compose.material3:material3:1.2.1")
// Accompanist Permissions
    implementation ("com.google.accompanist:accompanist-permissions:0.28.0")

// Activity Compose (для разрешений)
    implementation ("androidx.activity:activity-compose:1.7.2")

    implementation("com.yandex.android:maps.mobile:4.6.1-lite")

// Для ResponseBody (если нужно):
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")


    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}