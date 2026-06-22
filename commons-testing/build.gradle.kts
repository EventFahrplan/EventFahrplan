import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "info.metadude.android.eventfahrplan.commons.testing"

    compileSdk = config.versions.compile.sdk.get().toInteger()
    buildToolsVersion = config.versions.build.tools.get()

    defaultConfig {
        minSdk = config.versions.min.sdk.get().toInteger()
        targetSdk = config.versions.target.sdk.get().toInteger()
    }

    compileOptions {
        targetCompatibility = JavaVersion.toVersion(config.versions.java.get())
        sourceCompatibility = JavaVersion.toVersion(config.versions.java.get())
    }

    packagingOptions {
        resources {
            excludes += [
                    "META-INF/LICENSE.md",
                    "META-INF/LICENSE-notice.md",
            ]
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(config.versions.java.get())
        optIn.add("kotlin.RequiresOptIn")
    }
}

dependencies {
    implementation(libs.junit.jupiter.api)
    implementation(libs.kotlin.coroutines.test) {
        // workaround for https://github.com/Kotlin/kotlinx.coroutines/issues/2023
        exclude(group: "org.jetbrains.kotlinx", module: "kotlinx-coroutines-debug")
    }
    implementation(libs.truth)
    api(libs.mockito.kotlin)
}
