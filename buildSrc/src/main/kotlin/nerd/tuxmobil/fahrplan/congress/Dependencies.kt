@file:Suppress("unused", "ConstPropertyName")

package nerd.tuxmobil.fahrplan.congress

import org.gradle.api.JavaVersion

object Config {
    val compatibleJavaVersion = JavaVersion.VERSION_17
}

object Android {
    const val buildToolsVersion = "35.0.0"
    const val compileSdkVersion = 34
    const val minSdkVersion = 21
    const val targetSdkVersion = 34
}

object Compose {

    private object Versions {
        const val bom = "2024.11.00"
    }

    const val bom = "androidx.compose:compose-bom:${Versions.bom}"
    const val material = "androidx.compose.material3:material3"
    const val uiTooling = "androidx.compose.ui:ui-tooling"
    const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview"

}

object Plugins {

    private object Versions {
        const val android = "8.7.2"
        const val dexcount = "4.0.0"
        const val kotlin = "2.0.21"
        const val ksp = "2.0.21-1.0.28"
        const val sonarQube = "5.1.0.4882"
        const val unMock = "0.9.0"
        const val versions = "0.51.0"
    }

    const val android = "com.android.tools.build:gradle:${Versions.android}"
    const val composeCompiler = "org.jetbrains.kotlin:compose-compiler-gradle-plugin:${Versions.kotlin}"
    const val dexcount = "com.getkeepsafe.dexcount:dexcount-gradle-plugin:${Versions.dexcount}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val ksp = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${Versions.ksp}"
    const val sonarQube = "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:${Versions.sonarQube}"
    const val unMock = "com.github.bjoernq:unmockplugin:${Versions.unMock}"
    const val versions = "com.github.ben-manes:gradle-versions-plugin:${Versions.versions}"
}

object Libs {

    private object Versions {
        const val activityCompose = "1.9.3"
        const val androidTest = "1.6.0"
        const val annotation = "1.9.1"
        const val appCompat = "1.7.0"
        const val betterLinkMovementMethod = "2.2.0"
        const val constraintLayout = "2.2.0"
        const val coreKtx = "1.13.1" // compileSdk 35 is required as of 1.15.0
        const val coreTesting = "2.2.0"
        const val emailIntentBuilder = "2.0.0"
        const val engelsystem = "9.2.0"
        const val fragmentCompose = "1.8.5"
        const val junitJupiter = "5.11.3"
        const val kotlinCoroutines = "1.9.0"
        const val lifecycle = "2.8.7"
        const val markwon = "4.6.2"
        const val material = "1.12.0"
        const val mockito = "5.14.2"
        const val mockitoKotlin = "5.4.0"
        const val moshi = "1.15.1"
        const val okhttp = "4.12.0"
        const val preference = "1.2.1"
        const val retrofit = "2.11.0"
        const val robolectric = "4.3_r2-robolectric-0"
        const val snackengage = "0.30"
        const val threeTenBp = "1.7.0"
        const val tracedroid = "3.1"
        const val truth = "1.4.4"
        const val turbine = "1.2.0"
    }

    const val activityCompose = "androidx.activity:activity-compose:${Versions.activityCompose}"
    const val androidTestCore = "de.mannodermaus.junit5:android-test-core:${Versions.androidTest}"
    const val androidTestRunner = "de.mannodermaus.junit5:android-test-runner:${Versions.androidTest}"
    const val annotation = "androidx.annotation:annotation:${Versions.annotation}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val betterLinkMovementMethod = "me.saket:better-link-movement-method:${Versions.betterLinkMovementMethod}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val coreTesting = "androidx.arch.core:core-testing:${Versions.coreTesting}"
    const val emailIntentBuilder = "de.cketti.mailto:email-intent-builder:${Versions.emailIntentBuilder}"
    const val engelsystem = "info.metadude.kotlin.library.engelsystem:engelsystem-base:${Versions.engelsystem}"
    const val fragmentCompose = "androidx.fragment:fragment-compose:${Versions.fragmentCompose}"
    const val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:${Versions.junitJupiter}"
    const val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junitJupiter}"
    const val junitJupiterParams = "org.junit.jupiter:junit-jupiter-params:${Versions.junitJupiter}"
    const val kotlinCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}"
    const val kotlinCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
    const val kotlinCoroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutines}"
    const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val lifecycleRuntime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    const val markwonCore = "io.noties.markwon:core:${Versions.markwon}"
    const val markwonLinkify = "io.noties.markwon:linkify:${Versions.markwon}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val mockitoCore = "org.mockito:mockito-core:${Versions.mockito}"
    const val mockitoKotlin = "org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}"
    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    const val moshiCodeGen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val okhttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val okhttpMockWebServer = "com.squareup.okhttp3:mockwebserver:${Versions.okhttp}"
    const val preference = "androidx.preference:preference-ktx:${Versions.preference}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofitConverterMoshi = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
    const val robolectric = "org.robolectric:android-all:${Versions.robolectric}"
    const val snackengagePlayrate = "com.github.ligi.snackengage:snackengage-playrate:${Versions.snackengage}"
    const val threeTenBp = "org.threeten:threetenbp:${Versions.threeTenBp}"
    const val tracedroid = "com.github.ligi:tracedroid:${Versions.tracedroid}"
    const val truth = "com.google.truth:truth:${Versions.truth}"
    const val turbine = "app.cash.turbine:turbine:${Versions.turbine}"
}
