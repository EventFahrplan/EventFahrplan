@file:Suppress("unused", "ConstPropertyName")

package nerd.tuxmobil.fahrplan.congress

import org.gradle.api.JavaVersion

object Config {
    val compatibleJavaVersion = JavaVersion.VERSION_17
}

object Android {
    const val buildToolsVersion = "36.0.0"
    const val compileSdkVersion = 35
    const val minSdkVersion = 21
    const val targetSdkVersion = 35
}

object Compose {

    private object Versions {
        const val bom = "2025.11.00"
    }

    const val bom = "androidx.compose:compose-bom:${Versions.bom}"
    const val material = "androidx.compose.material3:material3"
    const val uiTooling = "androidx.compose.ui:ui-tooling"
    const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview"

}

object Plugins {

    private object Versions {
        const val android = "8.13.0"
        const val dexcount = "4.0.0"
        const val kotlin = "2.2.21"
        const val ksp = "2.3.2"
        const val sonarQube = "6.3.1.5724"
        const val unMock = "0.9.0"
        const val versions = "0.53.0"
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
        const val activityCompose = "1.10.1" // compileSdk 36 is required as of 1.11.0
        const val androidTest = "1.9.0"
        const val annotation = "1.9.1"
        const val appCompat = "1.7.1"
        const val betterLinkMovementMethod = "2.2.0"
        const val constraintLayout = "2.2.1"
        const val coreKtx = "1.16.0" // compileSdk 36 is required as of 1.17.0
        const val coreTesting = "2.2.0"
        const val emailIntentBuilder = "2.0.0"
        const val engelsystem = "11.0.0"
        const val fragmentCompose = "1.8.9"
        const val htmlConverter = "1.1.0"
        const val junitJupiter = "5.14.0"
        const val junitPlatform = "1.14.0"
        const val kotlinCoroutines = "1.10.2"
        const val lifecycle = "2.9.4" // minSdk 23 is required as of 2.10.0
        const val markdownRenderer = "0.38.1"
        const val material = "1.13.0"
        const val materialIconsCore = "1.7.8"
        const val mockitoKotlin = "6.1.0"
        const val moshi = "1.15.2"
        const val navigation = "2.9.2"
        const val okhttp = "5.3.0"
        const val robolectric = "4.3_r2-robolectric-0"
        const val roomStates = "2.0.0"
        const val snackengage = "0.30"
        const val threeTenBp = "1.7.2"
        const val tracedroid = "3.1"
        const val truth = "1.4.4" // NoSuchFieldError as of 1.4.5, see https://github.com/google/truth/issues/1608
        const val turbine = "1.2.1"
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
    const val engelsystem = "info.metadude.kotlin.library.engelsystem:engelsystem-repositories:${Versions.engelsystem}"
    const val fragmentCompose = "androidx.fragment:fragment-compose:${Versions.fragmentCompose}"
    const val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:${Versions.junitJupiter}"
    const val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junitJupiter}"
    const val junitJupiterParams = "org.junit.jupiter:junit-jupiter-params:${Versions.junitJupiter}"
    const val junitPlatformLauncher = "org.junit.platform:junit-platform-launcher:${Versions.junitPlatform}"
    const val htmlConverter = "be.digitalia.compose.htmlconverter:htmlconverter:${Versions.htmlConverter}"
    const val kotlinCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}"
    const val kotlinCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
    const val kotlinCoroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutines}"
    const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val lifecycleViewModelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose-android:${Versions.lifecycle}"
    const val lifecycleRuntime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    const val markdownRender = "com.mikepenz:multiplatform-markdown-renderer-m3:${Versions.markdownRenderer}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val materialIconsCore = "androidx.compose.material:material-icons-core:${Versions.materialIconsCore}"
    const val mockitoKotlin = "org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}"
    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    const val moshiCodeGen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
    const val navigation = "androidx.navigation:navigation-compose:${Versions.navigation}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val okhttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val robolectric = "org.robolectric:android-all:${Versions.robolectric}"
    const val roomStates = "info.metadude.kotlin.library.roomstates:room-states-repositories:${Versions.roomStates}"
    const val snackengagePlayrate = "com.github.ligi.snackengage:snackengage-playrate:${Versions.snackengage}"
    const val threeTenBp = "org.threeten:threetenbp:${Versions.threeTenBp}"
    const val tracedroid = "com.github.ligi:tracedroid:${Versions.tracedroid}"
    const val truth = "com.google.truth:truth:${Versions.truth}"
    const val turbine = "app.cash.turbine:turbine:${Versions.turbine}"
}
