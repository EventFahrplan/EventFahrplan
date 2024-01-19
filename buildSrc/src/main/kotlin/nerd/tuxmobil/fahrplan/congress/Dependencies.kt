@file:Suppress("unused")

package nerd.tuxmobil.fahrplan.congress

import org.gradle.api.JavaVersion

object Config {
    val compatibleJavaVersion = JavaVersion.VERSION_11
}

object Android {
    const val buildToolsVersion = "34.0.0"
    const val compileSdkVersion = 33
    const val minSdkVersion = 21
    const val targetSdkVersion = 33
}

object Compose {

    object Versions {
        internal const val bom = "2023.06.01" // compileSdk 34 is required as of 2023.08.00
        const val compiler = "1.5.6"
    }

    const val bom = "androidx.compose:compose-bom:${Versions.bom}"
    const val material = "androidx.compose.material3:material3"
    const val uiTooling = "androidx.compose.ui:ui-tooling"
    const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview"

}

object Plugins {

    private object Versions {
        const val android = "7.4.2"
        const val androidJunitJacoco = "0.16.0"
        const val dexcount = "4.0.0"
        const val kotlin = "1.9.21"
        const val ksp = "1.9.21-1.0.15"
        const val sonarQube = "4.4.1.3373"
        const val unMock = "0.7.9"
        const val versions = "0.50.0"
    }

    const val android = "com.android.tools.build:gradle:${Versions.android}"
    const val androidJunitJacoco = "com.vanniktech:gradle-android-junit-jacoco-plugin:${Versions.androidJunitJacoco}"
    const val dexcount = "com.getkeepsafe.dexcount:dexcount-gradle-plugin:${Versions.dexcount}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val ksp = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${Versions.ksp}"
    const val sonarQube = "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:${Versions.sonarQube}"
    const val unMock = "com.github.bjoernq:unmockplugin:${Versions.unMock}"
    const val versions = "com.github.ben-manes:gradle-versions-plugin:${Versions.versions}"
}

object Libs {

    private object Versions {
        const val annotation = "1.7.0"
        const val appCompat = "1.6.1"
        const val assertjAndroid = "1.2.0"
        const val betterLinkMovementMethod = "2.2.0"
        const val constraintLayout = "2.1.4"
        const val coreKtx = "1.9.0"
        const val coreTesting = "2.2.0"
        const val emailIntentBuilder = "2.0.0"
        const val engelsystem = "8.0.0"
        const val espresso = "3.5.1"
        const val junit = "4.13.2"
        const val kotlinCoroutines = "1.7.3"
        const val lifecycle = "2.6.2"
        const val markwon = "4.6.2"
        const val material = "1.9.0"
        const val mockito = "5.9.0"
        const val mockitoKotlin = "5.2.1"
        const val moshi = "1.15.0"
        const val okhttp = "4.12.0"
        const val preference = "1.2.1"
        const val retrofit = "2.9.0"
        const val robolectric = "4.3_r2-robolectric-0"
        const val snackengage = "0.30"
        const val testExtJunit = "1.1.5"
        const val threeTenBp = "1.6.8"
        const val tracedroid = "3.1"
        const val truth = "1.2.0"
        const val turbine = "1.0.0"
    }

    const val annotation = "androidx.annotation:annotation:${Versions.annotation}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val assertjAndroid = "com.squareup.assertj:assertj-android:${Versions.assertjAndroid}"
    const val betterLinkMovementMethod = "me.saket:better-link-movement-method:${Versions.betterLinkMovementMethod}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val coreTesting = "androidx.arch.core:core-testing:${Versions.coreTesting}"
    const val emailIntentBuilder = "de.cketti.mailto:email-intent-builder:${Versions.emailIntentBuilder}"
    const val engelsystem = "info.metadude.kotlin.library.engelsystem:engelsystem-base:${Versions.engelsystem}"
    const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val junit = "junit:junit:${Versions.junit}"
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
    const val testExtJunit = "androidx.test.ext:junit:${Versions.testExtJunit}"
    const val threeTenBp = "org.threeten:threetenbp:${Versions.threeTenBp}"
    const val tracedroid = "com.github.ligi:tracedroid:${Versions.tracedroid}"
    const val truth = "com.google.truth:truth:${Versions.truth}"
    const val turbine = "app.cash.turbine:turbine:${Versions.turbine}"
}
