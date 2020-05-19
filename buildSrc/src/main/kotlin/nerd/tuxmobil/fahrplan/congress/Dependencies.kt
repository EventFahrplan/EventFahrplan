@file:Suppress("unused")

package nerd.tuxmobil.fahrplan.congress

object Android {
    const val buildToolsVersion = "29.0.3"
    const val compileSdkVersion = 28
    const val minSdkVersion = 14
    const val targetSdkVersion = 28
}

private const val kotlinVersion = "1.3.72"

object Plugins {

    private object Versions {
        const val android = "4.0.1"
        const val androidJunitJacoco = "0.16.0"
        const val sonarQube = "3.0"
        const val unMock = "0.7.6"
        const val versions = "0.29.0"
    }

    const val android = "com.android.tools.build:gradle:${Versions.android}"
    const val androidJunitJacoco = "com.vanniktech:gradle-android-junit-jacoco-plugin:${Versions.androidJunitJacoco}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val sonarQube = "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:${Versions.sonarQube}"
    const val unMock = "de.mobilej.unmock:UnMockPlugin:${Versions.unMock}"
    const val versions = "com.github.ben-manes:gradle-versions-plugin:${Versions.versions}"
}

object Libs {

    private object Versions {
        const val annotation = "1.1.0"
        const val appCompat = "1.2.0"
        const val assertjAndroid = "1.2.0"
        const val betterLinkMovementMethod = "2.2.0" // minSdkVersion 16, see AndroidManifest
        const val constraintLayout = "1.1.3"
        const val coreKtx = "1.3.1"
        const val emailIntentBuilder = "2.0.0"
        const val engelsystem = "4.0.1"
        const val espresso = "3.2.0"
        const val junit = "4.13"
        const val kotlinCoroutines = "1.3.8"
        const val material = "1.2.0"
        const val mockito = "3.4.0"
        const val mockitoKotlin = "2.2.0"
        const val moshi = "1.9.3"
        const val okhttp = "3.12.12"
        const val retrofit = "2.6.4"
        const val robolectric = "4.3_r2-robolectric-0"
        const val snackengage = "0.26"
        const val testExtJunit = "1.1.1"
        const val threeTenBp = "1.4.4"
        const val tracedroid = "1.4"
        const val truth = "1.0.1"
    }

    const val annotation = "androidx.annotation:annotation:${Versions.annotation}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val assertjAndroid = "com.squareup.assertj:assertj-android:${Versions.assertjAndroid}"
    const val betterLinkMovementMethod = "me.saket:better-link-movement-method:${Versions.betterLinkMovementMethod}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val emailIntentBuilder = "de.cketti.mailto:email-intent-builder:${Versions.emailIntentBuilder}"
    const val engelsystem = "info.metadude.kotlin.library.engelsystem:engelsystem-base:${Versions.engelsystem}"
    const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val junit = "junit:junit:${Versions.junit}"
    const val kotlinCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}"
    const val kotlinCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val mockitoCore = "org.mockito:mockito-core:${Versions.mockito}"
    const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}"
    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    const val moshiCodeGen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val okhttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val okhttpMockWebServer = "com.squareup.okhttp3:mockwebserver:${Versions.okhttp}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofitConverterMoshi = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
    const val robolectric = "org.robolectric:android-all:${Versions.robolectric}"
    const val snackengagePlayrate = "com.github.ligi.snackengage:snackengage-playrate:${Versions.snackengage}"
    const val testExtJunit = "androidx.test.ext:junit:${Versions.testExtJunit}"
    const val threeTenBp = "org.threeten:threetenbp:${Versions.threeTenBp}"
    const val tracedroid = "org.ligi:tracedroid:${Versions.tracedroid}"
    const val truth = "com.google.truth:truth:${Versions.truth}"
}
