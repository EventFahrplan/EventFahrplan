@file:Suppress("unused")

package nerd.tuxmobil.fahrplan.congress

object Android {
    const val buildToolsVersion = "29.0.1"
    const val compileSdkVersion = 28
    const val minSdkVersion = 14
    const val targetSdkVersion = 28
}

private const val kotlinVersion = "1.3.50"

object GradlePlugins {

    private object Versions {
        const val androidGradle = "3.4.2"
        const val gradleVersions = "0.25.0"
        const val sonarQubeGradle = "2.7.1"
    }

    const val androidGradle = "com.android.tools.build:gradle:${Versions.androidGradle}"
    const val gradleVersions = "com.github.ben-manes:gradle-versions-plugin:${Versions.gradleVersions}"
    const val sonarQubeGradle = "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:${Versions.sonarQubeGradle}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
}

object Libs {

    private object Versions {
        const val assertjAndroid = "1.2.0"
        const val constraintLayout = "1.1.3"
        const val emailIntentBuilder = "1.0.0"
        const val espresso = "3.0.2"
        const val junit = "4.12"
        const val mockito = "2.28.0"
        const val mockitoKotlin = "2.2.0"
        const val okhttp = "3.12.4"
        const val snackengage = "0.22"
        const val supportLibrary = "28.0.0"
        const val testRules = "1.0.2"
        const val threeTenBp = "1.4.0"
        const val tracedroid = "1.4"
    }

    const val assertjAndroid = "com.squareup.assertj:assertj-android:${Versions.assertjAndroid}"
    const val emailIntentBuilder = "de.cketti.mailto:email-intent-builder:${Versions.emailIntentBuilder}"
    const val espresso = "com.android.support.test.espresso:espresso-core:${Versions.espresso}"
    const val junit = "junit:junit:${Versions.junit}"
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
    const val mockitoCore = "org.mockito:mockito-core:${Versions.mockito}"
    const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val okhttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val snackengagePlayrate = "com.github.ligi.snackengage:snackengage-playrate:${Versions.snackengage}"
    const val supportLibraryAnnotations = "com.android.support:support-annotations:${Versions.supportLibrary}"
    const val supportLibraryAppcompatV7 = "com.android.support:appcompat-v7:${Versions.supportLibrary}"
    const val supportLibraryConstraintLayout = "com.android.support.constraint:constraint-layout:${Versions.constraintLayout}"
    const val supportLibraryDesign = "com.android.support:design:${Versions.supportLibrary}"
    const val testRules = "com.android.support.test:rules:${Versions.testRules}"
    const val threeTenBp = "org.threeten:threetenbp:${Versions.threeTenBp}"
    const val tracedroid = "org.ligi:tracedroid:${Versions.tracedroid}"
}
