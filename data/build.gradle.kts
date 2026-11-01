import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.serialization)
}
kotlin {
    jvmToolchain(17)
    androidLibrary {
        namespace = "app.yongin.xr_circuit.data"
        compileSdk = 36
        minSdk = 34
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
        withHostTestBuilder {}.configure {}
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "CircuitData"
            isStatic = true
            export(project(":domain"))
        }
    }
    sourceSets {
        if (providers.gradleProperty("stage3HttpIntegration").isPresent) {
            getByName("iosSimulatorArm64Test").kotlin.srcDir("src/iosIntegrationTest/kotlin")
        }
        commonMain.dependencies {
            api(project(":domain"))
            implementation(libs.kotlinx.serialization.json)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation("io.ktor:ktor-client-core:3.0.3")
        }
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:3.0.3")
            implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
        }
        getByName("androidHostTest").dependencies { implementation("com.squareup.okhttp3:mockwebserver:4.12.0") }
        iosMain.dependencies { implementation("io.ktor:ktor-client-darwin:3.0.3") }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
            implementation("io.ktor:ktor-client-mock:3.0.3")
        }
    }
}
