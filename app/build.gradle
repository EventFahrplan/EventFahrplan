import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.unmock)
    alias(libs.plugins.dexcount)
    alias(libs.plugins.sonarqube)
}

ext.set("APP_VERSION", "${gitSha()}")

apply(from: "../gradle/sonarqube.gradle")

android {
    namespace = "nerd.tuxmobil.fahrplan.congress"

    compileSdk = config.versions.compile.sdk.get().toInteger()
    buildToolsVersion = config.versions.build.tools.get()

    defaultConfig {
        versionCode = 119
        versionName = "1.76.0"
        minSdk = config.versions.min.sdk.get().toInteger()
        targetSdk = config.versions.target.sdk.get().toInteger()
        base.archivesName = "Fahrplan-$versionName"

        vectorDrawables.useSupportLibrary = true // allows using fillColor, fillType, strokeColor functionalities below Android 7.0 (API 24)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments(runnerBuilder: "de.mannodermaus.junit5.AndroidJUnit5Builder")

        // Build information
        resValue("string", "modification_time", "\"${modificationTime()}\"")
        resValue("string", "git_sha", "\"${gitSha()}\"")

        // Build configuration / feature flags
        buildConfigField("String", "F_DROID_URL", '""')
        buildConfigField("String", "TRANSLATION_PLATFORM_URL", '"https://crowdin.com/project/eventfahrplan"')
        buildConfigField("String", "SOURCE_CODE_URL", '"https://github.com/EventFahrplan/EventFahrplan"')
        buildConfigField("String", "ISSUES_URL", '"https://github.com/EventFahrplan/EventFahrplan/issues"')
        buildConfigField("String", "DATA_PRIVACY_STATEMENT_DE_URL", '"https://github.com/EventFahrplan/EventFahrplan/blob/master/DATA-PRIVACY-DE.md"')
        buildConfigField("String", "EVENT_POSTAL_ADDRESS", '""')
        buildConfigField("boolean", "ENGAGE_C3NAV_APP_INSTALLATION", "false")
        buildConfigField("String", "C3NAV_URL", '""')
        buildConfigField("boolean", "ENABLE_ALTERNATIVE_SCHEDULE_URL", "false")
        buildConfigField("boolean", "ENABLE_CHAOSFLIX_EXPORT", "false")
        buildConfigField("boolean", "ENABLE_ENGELSYSTEM_SHIFTS", "false")
        resValue("string", "engelsystem_alias", "Engelsystem")
        resValue("string", "engelsystem_shifts_alias", "Engelshifts")
        resValue("string", "preference_hint_engelsystem_json_export_url", '""')
        buildConfigField("boolean", "SHOW_APP_DISCLAIMER", "true")
        buildConfigField("boolean", "ENGAGE_GOOGLE_BETA_TESTING", "true")
        buildConfigField("boolean", "ENGAGE_GOOGLE_PLAY_RATING", "true")
        buildConfigField("boolean", "ENGAGE_LANDSCAPE_ORIENTATION", "true")
        buildConfigField("boolean", "ENABLE_FOSDEM_ROOM_STATES", "false")
        buildConfigField("String", "FOSDEM_ROOM_STATES_URL", '""')
        buildConfigField("String", "FOSDEM_ROOM_STATES_PATH", '""')
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        debug {
            resValue("string", "build_time", "\"${buildTime()}\"")
            versionNameSuffix = "-DEBUG"
            applicationIdSuffix = ".debug"
            debuggable = true
        }
        release {
            resValue("string", "build_time", "")
            minifyEnabled = true
            shrinkResources = true
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules/proguard-project.txt",
                    "proguard-rules/okhttp3.pro",
                    "proguard-rules/okio.pro"
            )
        }
    }

    signingConfigs {
        cccamp2023
        ccc39c3
    }

    def defaultDimension = "default"
    flavorDimensions = [defaultDimension]

    productFlavors {
        cccamp2023 {
            dimension = defaultDimension
            applicationId = "info.metadude.android.cccamp.schedule"
            versionName = "${defaultConfig.versionName}-CCCamp-Edition"
            buildConfigField("String", "GOOGLE_PLAY_URL", '"https://play.google.com/store/apps/details?id=info.metadude.android.cccamp.schedule"')
            buildConfigField("String", "F_DROID_URL", '"https://f-droid.org/packages/info.metadude.android.cccamp.schedule"')
            buildConfigField("String", "SCHEDULE_URL", '"https://events.ccc.de/camp/2023/hub/api/c/camp23/schedule.xml"')
            buildConfigField("String", "SCHEDULE_FILE_FORMAT", '"schedule_v1_xml"')
            buildConfigField("String", "EVENT_URL", '"http://events.ccc.de/camp/2023/Fahrplan/events/%1$s.html"')
            buildConfigField("String", "EVENT_WEBSITE_URL", '"http://events.ccc.de/camp/2023/"')
            buildConfigField("String", "EVENT_POSTAL_ADDRESS", '"Ziegeleipark Mildenberg, 16792 Zehdenick"')
            buildConfigField("String", "SERVER_BACKEND_TYPE", '"frab"')
            buildConfigField("boolean", "SHOW_APP_DISCLAIMER", "false")
            buildConfigField("boolean", "ENGAGE_C3NAV_APP_INSTALLATION", "true")
            buildConfigField("String", "C3NAV_URL", '"https://camp23.c3nav.de/l/"')
            buildConfigField("boolean", "ENABLE_ALTERNATIVE_SCHEDULE_URL", "true")
            buildConfigField("boolean", "ENABLE_CHAOSFLIX_EXPORT", "true")
            buildConfigField("boolean", "ENABLE_ENGELSYSTEM_SHIFTS", "true")
            resValue("string", "preference_hint_engelsystem_json_export_url", '"https://engelsystem.events.ccc.de/shifts-json-export?key=YOUR_KEY"')
            buildConfigField("String", "SOCIAL_MEDIA_HASHTAGS_HANDLES", '"#camp23 #CCCamp23 #fahrplan"')
            buildConfigField("String", "TRACE_DROID_EMAIL_ADDRESS", '"tobias.preuss+camp2023@googlemail.com"')
            buildConfigField("String", "SCHEDULE_FEEDBACK_URL", '"https://frab.cccv.de/en/camp2023/public/events/%s/feedback/new"')
        }
        ccc39c3 {
            dimension = defaultDimension
            applicationId = "info.metadude.android.congress.schedule"
            buildConfigField("String", "GOOGLE_PLAY_URL", '"https://play.google.com/store/apps/details?id=info.metadude.android.congress.schedule"')
            buildConfigField("String", "F_DROID_URL", '"https://f-droid.org/packages/info.metadude.android.congress.schedule"')
            buildConfigField("String", "SCHEDULE_URL", '"https://fahrplan.events.ccc.de/congress/2025/fahrplan/schedules/schedule.xml"')
            buildConfigField("String", "SCHEDULE_FILE_FORMAT", '"schedule_v1_xml"')
            buildConfigField("String", "EVENT_URL", '""')
            buildConfigField("String", "EVENT_WEBSITE_URL", '"https://events.ccc.de/congress/2025/"')
            buildConfigField("String", "EVENT_POSTAL_ADDRESS", '"Congressplatz 1, 20355 Hamburg"')
            buildConfigField("String", "SERVER_BACKEND_TYPE", '"frab"')
            buildConfigField("boolean", "SHOW_APP_DISCLAIMER", "false")
            buildConfigField("boolean", "ENGAGE_C3NAV_APP_INSTALLATION", "true")
            buildConfigField("String", "C3NAV_URL", '"https://39c3.c3nav.de/l/"')
            buildConfigField("boolean", "ENABLE_ALTERNATIVE_SCHEDULE_URL", "true")
            buildConfigField("boolean", "ENABLE_CHAOSFLIX_EXPORT", "true")
            buildConfigField("boolean", "ENABLE_ENGELSYSTEM_SHIFTS", "true")
            resValue("string", "preference_hint_engelsystem_json_export_url", '"https://engel.events.ccc.de/shifts-json-export?key=YOUR_KEY"')
            buildConfigField("String", "SOCIAL_MEDIA_HASHTAGS_HANDLES", '"#39c3 #fahrplan"')
            buildConfigField("String", "TRACE_DROID_EMAIL_ADDRESS", '"tobias.preuss+39c3@googlemail.com"')
            buildConfigField("String", "SCHEDULE_FEEDBACK_URL", '""')
        }
    }

    productFlavors.configureEach { flavor ->
        if (hasSigningConfig(flavor.name)) {
            setSigningConfig(flavor)
        }
    }

    lint {
        checkDependencies = true
        // for okio - https://github.com/square/okio/issues/58
        warning("InvalidPackage")
        warning("MissingDefaultResource")
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

unMock {
    keepStartingWith("libcore.")
    keepStartingWith("org.ccil.cowan.tagsoup.")
    keep("android.content.ComponentName")
    keep("android.content.ContentValues")
    keep("android.content.Intent")
    keep("android.net.Uri")
    keepStartingWith("android.text.")
    keep("android.os.Bundle")
    keep("android.util.Patterns")
    keep("com.android.internal.util.ArrayUtils")
    keepAndRename("java.nio.charset.Charsets").to("xjava.nio.charset.Charsets")
}

gradle.projectsEvaluated {
    tasks.withType(JavaCompile).configureEach {
        options.compilerArgs << "-Xlint:unchecked" << "-Xlint:deprecation"
    }
}

dependencies {
    implementation(project(":commons"))
    implementation(project(":database"))
    implementation(project(":network"))

    def composeBom = platform(libs.compose.bom)
    implementation(composeBom)

    // Android Studio preview support
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.compose.material3)

    implementation(libs.activity.compose)
    implementation(libs.appcompat)
    implementation(libs.better.link.movement.method)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.email.intent.builder)
    implementation(libs.engelsystem)
    implementation(libs.fragment.compose)
    implementation(libs.htmlconverter)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.markdown.renderer)
    implementation(libs.material)
    implementation(libs.material.icons.core)
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)
    implementation(libs.navigation)
    implementation(libs.okhttp)
    implementation(libs.okhttp.zstd)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.room.states)
    implementation(libs.schedule.repositories)
    implementation(libs.snackengage.playrate)
    implementation(libs.tracedroid)

    testImplementation(project(":commons-testing"))
    testImplementation(libs.annotation)
    testImplementation(libs.core.testing)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.kotlin.coroutines.test) {
        // workaround for https://github.com/Kotlin/kotlinx.coroutines/issues/2023
        exclude(group: "org.jetbrains.kotlinx", module: "kotlinx-coroutines-debug")
    }
    testImplementation(libs.threetenbp)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)

    unmock(libs.robolectric)

    androidTestImplementation(libs.android.test.core)
    androidTestRuntimeOnly(libs.android.test.runner)
    androidTestImplementation(libs.junit.jupiter.api)
    androidTestImplementation(libs.junit.jupiter.params)
    androidTestImplementation(libs.truth)
}

def gitSha() {
    def res = "git rev-parse --short HEAD".execute([], project.rootDir).text.trim()
    def diff = "git diff".execute([], project.rootDir).text.trim()
    if (diff != null && diff.length() > 0) {
        res += "-dirty"
    }
    return res
}

def modificationTime() {
    // https://reproducible-builds.org/docs/source-date-epoch/
    // Set based on the current commit that is being built.
    def committedAt = System.getenv('SOURCE_DATE_EPOCH')
    if (committedAt == null || committedAt.length() == 0) {
        committedAt = "git log -1 --format=%ct".execute([], project.rootDir).text.trim()
    }
    return getFormattedDate(new Date(Long.parseLong(committedAt) * 1000))
}

static def buildTime() {
    return getFormattedDate(new Date())
}

static def getFormattedDate(Date date) {
    return date.format("yyyy-MM-dd'T'HH:mm'Z'", TimeZone.getTimeZone("UTC"))
}

def hasSigningConfig(String flavor) {
    return project.hasProperty("signing.$flavor-release.keystoreFilePath") &&
            project.hasProperty("signing.$flavor-release.keystorePassword") &&
            project.hasProperty("signing.$flavor-release.keyAlias") &&
            project.hasProperty("signing.$flavor-release.keyPassword")
}

def setSigningConfig(flavor) {
    def flavorName = flavor.name
    def props = project.getProperties()

    def signingConfig = android.signingConfigs[flavorName]
    signingConfig.storeFile(file(props["signing.$flavorName-release.keystoreFilePath"]))
    signingConfig.storePassword(props["signing.$flavorName-release.keystorePassword"])
    signingConfig.keyAlias(props["signing.$flavorName-release.keyAlias"])
    signingConfig.keyPassword(props["signing.$flavorName-release.keyPassword"])

    flavor.signingConfig = signingConfig
}
