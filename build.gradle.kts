import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dexcount) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.sonarqube) apply false
    alias(libs.plugins.unmock) apply false
    alias(libs.plugins.versions)
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

subprojects {
    val mockitoAgent = configurations.create("mockitoAgent")
    dependencies {
        mockitoAgent(rootProject.libs.mockito.core) { isTransitive = false }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform {
            // Roborazzi preview tests and junit-vintage-engine live only in :app; filtering
            // other modules with -Pscreenshot would match no tests (failOnNoMatchingTests).
            if (project.name == "app") {
                val previewTestTag = "com.github.takahirom.roborazzi.RoborazziComposePreviewTestCategory"
                val taskPrefixes = listOf("recordRoborazzi", "verifyRoborazzi", "compareRoborazzi")
                val isRoborazziTaskInvocation = gradle.startParameter.taskNames.any { taskName ->
                    taskPrefixes.any { it in taskName }
                }
                if (project.hasProperty("screenshot") || isRoborazziTaskInvocation) {
                    includeTags(previewTestTag)
                } else {
                    // Keep regular unit test runs free of the slow Robolectric preview tests.
                    excludeTags(previewTestTag)
                }
            }
        }
        jvmArgumentProviders.add(CommandLineArgumentProvider {
            listOf("-javaagent:${mockitoAgent.singleFile}")
        })
        testLogging {
            events(
                TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STANDARD_OUT,
            )
            exceptionFormat = TestExceptionFormat.FULL
            showCauses = true
            showExceptions = true
            showStackTraces = true
        }
    }
}

apply(from = "gradle/versions.gradle")
