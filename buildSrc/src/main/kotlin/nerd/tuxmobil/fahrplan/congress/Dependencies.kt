@file:Suppress("unused")

package nerd.tuxmobil.fahrplan.congress

import org.gradle.api.JavaVersion

object Config {
    val compatibleJavaVersion = JavaVersion.VERSION_11
}

object Android {
    const val buildToolsVersion = "33.0.0"
    const val compileSdkVersion = 31
    const val minSdkVersion = 16
    const val targetSdkVersion = 31
}

object Plugins {

    private object Versions {
        const val android = "7.3.0"
        const val androidJunitJacoco = "0.16.0"
        const val dexcount = "3.1.0"
        const val kotlin = "1.7.10"
        const val ksp = "1.7.10-1.0.6"
        const val sonarQube = "3.4.0.2513"
        const val unMock = "0.7.9"
        const val versions = "0.42.0"
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
        const val annotation = "1.5.0"
        const val appCompat = "1.4.2" // compileSdk 32 is required as of 1.5.0
        const val assertjAndroid = "1.2.0"
        const val betterLinkMovementMethod = "2.2.0"
        const val constraintLayout = "2.1.4"
        const val coreKtx = "1.8.0"
        const val coreTesting = "2.1.0"
        const val emailIntentBuilder = "2.0.0"
        const val engelsystem = "7.1.0"
        const val espresso = "3.4.0"
        const val junit = "4.13.2"
        const val kotlinCoroutines = "1.6.4"
        const val lifecycle = "2.5.1"
        const val markwon = "4.6.2"
        const val material = "1.6.1"
        const val mockito = "4.8.0"
        const val mockitoKotlin = "4.0.0"
        const val moshi = "1.13.0"
        const val multiDex = "2.0.1"
        const val okhttp = "3.12.13"
        const val preference = "1.2.0"
        const val retrofit = "2.6.4"
        const val robolectric = "4.3_r2-robolectric-0"
        const val snackengage = "0.29"
        const val testExtJunit = "1.1.3"
        const val threeTenBp = "1.6.1"
        const val tracedroid = "3.1"
        const val truth = "1.1.3"
        const val turbine = "0.11.0"
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
    const val lifecycleLiveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    const val markwonCore = "io.noties.markwon:core:${Versions.markwon}"
    const val markwonLinkify = "io.noties.markwon:linkify:${Versions.markwon}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val mockitoCore = "org.mockito:mockito-core:${Versions.mockito}"
    const val mockitoKotlin = "org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}"
    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    const val moshiCodeGen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
    const val multiDex = "androidx.multidex:multidex:${Versions.multiDex}"
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
