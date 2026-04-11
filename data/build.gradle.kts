plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "app.yongin.xr_circuit.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.core.ktx)
    implementation("com.squareup.retrofit2:retrofit:${libs.versions.retrofit.get()}")
    implementation("com.squareup.retrofit2:converter-gson:${libs.versions.retrofit.get()}")
    implementation("com.squareup.okhttp3:okhttp:${libs.versions.okhttp.get()}")
    implementation("com.squareup.okhttp3:logging-interceptor:${libs.versions.okhttp.get()}")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
