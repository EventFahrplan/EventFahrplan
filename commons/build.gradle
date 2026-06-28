import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "info.metadude.android.eventfahrplan.commons"

    compileSdk = config.versions.compile.sdk.get().toInteger()
    buildToolsVersion = config.versions.build.tools.get()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = config.versions.min.sdk.get().toInteger()
        targetSdk = config.versions.target.sdk.get().toInteger()
    }

    compileOptions {
        targetCompatibility = JavaVersion.toVersion(config.versions.java.get())
        sourceCompatibility = JavaVersion.toVersion(config.versions.java.get())
    }

}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(config.versions.java.get())
    }
}

dependencies {
    implementation(libs.lifecycle.runtime)
    implementation(libs.tracedroid)
    api(libs.threetenbp)

    testImplementation(project(":commons-testing"))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.truth)
}
