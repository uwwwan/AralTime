plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.araltime"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.araltime"
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ==================== Firebase ====================
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))

    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    // Remove any -ktx versions if you had them before (they are no longer needed)

    // ==================== Other dependencies ====================

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")   // updated
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")   // updated
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.1.0")               // updated

    // RecyclerView + CardView
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")  // updated

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Google Sign-In / Credentials
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}