plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation(libs.javax.inject)
    testImplementation(libs.junit)
}
