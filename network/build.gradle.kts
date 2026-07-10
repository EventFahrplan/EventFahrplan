import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "info.metadude.android.eventfahrplan.network"

    compileSdk = config.versions.compile.sdk.get().toInt()
    buildToolsVersion = config.versions.build.tools.get()

    defaultConfig {
        minSdk = config.versions.min.sdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
    }

    compileOptions {
        targetCompatibility = JavaVersion.toVersion(config.versions.java.get())
        sourceCompatibility = JavaVersion.toVersion(config.versions.java.get())
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(config.versions.java.get())
    }
}

dependencies {
    implementation(project(":commons"))

    implementation(libs.annotation)
    implementation(libs.core.ktx)
    implementation(libs.okhttp)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.truth)

    androidTestImplementation(libs.android.test.core)
    androidTestRuntimeOnly(libs.android.test.runner)
    androidTestImplementation(libs.junit.jupiter.api)
    androidTestImplementation(libs.truth)
}
