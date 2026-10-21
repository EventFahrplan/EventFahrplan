# Screenshot testing

All composable screen previews can be captured as screenshots. This allows comparing the rendering
a screen before and after a code change or a dependency update.

## Recording screenshots

Run `./gradlew recordRoborazzi[Flavor]Debug` to capture all screenshots.

The images are created under `app/src/test/screenshots/`.
Store these images as "golden" screenshots, so screenshots taken later can be compared or validated
against them.

An HTML report is generated under `app/build/reports/roborazzi/[Flavor]Debug/index.html`.

## Comparing screenshots

Run `./gradlew compareRoborazzi[Flavor]Debug` to compare old and new screenshots.

Review changes made to an image. This action will compare the current image with the saved one,
generating a comparison image labeled as `[original]_compare.png`.

## Verifying screenshots

Run `./gradlew verifyRoborazzi[Flavor]Debug` to verify all "golden" screenshots.

Validate changes made to an image. If there is any difference between the current image and the
saved one, the test will fail.

## References

Screenshot support is added through [Roborazzi](https://takahirom.github.io/roborazzi/).
