package nerd.tuxmobil.fahrplan.congress.screenshottesting

import com.github.takahirom.roborazzi.AndroidComposePreviewTester
import com.github.takahirom.roborazzi.AndroidComposePreviewTester.CaptureParameter
import com.github.takahirom.roborazzi.AndroidComposePreviewTester.Capturer
import com.github.takahirom.roborazzi.AndroidComposePreviewTester.DefaultCapturer
import com.github.takahirom.roborazzi.ComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester.TestParameter.JUnit4TestParameter.AndroidPreviewJUnit4TestParameter
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.inspectionMode

@OptIn(ExperimentalRoborazziApi::class)
class PackageStrippingComposePreviewTester : ComposePreviewTester<AndroidPreviewJUnit4TestParameter> by AndroidComposePreviewTester(
    capturer = PackageStrippingCapturer("nerd/tuxmobil/fahrplan/congress/"),
)

@OptIn(ExperimentalRoborazziApi::class)
private class PackageStrippingCapturer(val packagePrefix: String) : Capturer {

    private val capturer = DefaultCapturer()

    override fun capture(parameter: CaptureParameter) {
        capturer.capture(
            parameter.copy(
                filePath = parameter.filePath.removePrefix(packagePrefix),
                // Render like the Android Studio preview. Without inspection mode the Markdown
                // renderer parses asynchronously which makes screenshots flaky.
                roborazziComposeOptions = parameter.roborazziComposeOptions.builder()
                    .inspectionMode(true)
                    .build(),
            )
        )
    }
}
