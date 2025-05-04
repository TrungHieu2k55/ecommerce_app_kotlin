plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.example.duan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.duan"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose) // Đã là 1.9.0
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.mediation.test.suite)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.espresso.core)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.play.services.cast.tv)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.compose.material:material-icons-extended:1.6.8") // Cập nhật lên phiên bản mới nhất
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4") // Cập nhật lên phiên bản mới nhất
    implementation("com.google.firebase:firebase-auth:23.0.0") // Cập nhật lên phiên bản mới nhất
    implementation("com.google.android.gms:play-services-auth:21.2.0") // Cập nhật lên phiên bản mới nhất
    implementation("com.google.dagger:hilt-android:2.51.1") // Cập nhật lên phiên bản mới nhất
    kapt("com.google.dagger:hilt-android-compiler:2.51.1") // Phải khớp với hilt-android

    // Hilt ViewModel support
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Chỉ giữ phiên bản mới nhất của Facebook SDK để tránh xung đột
    implementation("com.facebook.android:facebook-android-sdk:16.3.0") // Cập nhật lên phiên bản mới nhất
    implementation("com.facebook.android:facebook-login:16.3.0")

    // Coil for Jetpack Compose
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Cloudinary
    implementation("com.cloudinary:cloudinary-android:3.0.2")

    // Ktor JSON
    implementation("io.ktor:ktor-client-core:2.3.12") // Cập nhật lên phiên bản mới nhất
    implementation("io.ktor:ktor-client-cio:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1") // Cập nhật lên phiên bản mới nhất

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1") // Cập nhật lên phiên bản mới nhất

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // Cập nhật lên phiên bản mới nhất
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("androidx.browser:browser:1.8.0")
}