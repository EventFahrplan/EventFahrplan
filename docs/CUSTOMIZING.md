# Customization guide

This document describes how to create your own app for an event.

## 1. Required information

The following information is required to configure the app for your event.
This list is for your preparation. Step 3 guides you through where to enter the information.

- Schedule URL which provides Frab compatible XML
- Session URL template, e.g. `https://awesome-event.com/2021/events/%1$s.html`
- Server backend type, one of: `pentabarf`, `frab`, `pretalx`
- Google Play URL, e.g. `https://play.google.com/store/apps/details?id=com.awesome.event.schedule`
- F-Droid URL, e.g. `https://f-droid.org/packages/com.awesome.event.schedule`
- Event URL, e.g. `https://awesome-event.com/2021`
- Event postal address (optional), e.g. `CCH, Congressplatz 1, 20355 Hamburg`
- Start and end date of the event
- Email address for bug reports
- Social media hashtags/handles (can be empty), e.g. `#36c3 @ccc`
- Schedule feedback URL template (optional), e.g. `https://awesome-event.com/2021/events/%s/feedback/new`
- Custom name used for the Engelsystem (optional), e.g. `Trollsystem`
- Custom name used for the Engelsystem shifts (optional), e.g. `Trollshifts`
- Engelsystem URL (optional), e.g. `https://engelsystem.de/awesome-event/shifts-json-export?key=YOUR_KEY`
- Name/s of the author/s of the logo(s), website URL/s optionally

## 2. Required graphic assets and colors

The following graphics and colors are required to customize the look and feel of the app.
This list is for your preparation. The specific folders and files are mentioned in the next step.

- Launcher icon (SVG)

  ![Launcher icon examples](gfx/launcher-icon.png)

- Notification icon (SVG)

  ![Notification icon example](gfx/notification-icon.png)

- About dialog logo (SVG)

  ![About dialog logo example](gfx/about-dialog-logo.png)

- Text and background colors (HEX)
- Tracks background colors (HEX)
- Icons in the toolbar as well as the session alarm icon (bell) can be customized via the `tool_bar_icon` color resource.
- The alarm icon (bell) which is shown on a session can be customized via the `session_item_alarm_icon` color resource.
- The video recording icons must be customized manually because they contain more than one color.

## 3. Your custom app step by step

In some of the steps it is the easiest to copy and adapt configuration settings, folders and files from an existing flavor.

1. Fork the repository
2. Create a new branch for your event, e.g. `awesome-event-2021`
3. Add a new product flavor in *app/build.gradle* e.g. `awesome2021` and the corresponding folder e.g. `app/src/awesome2021`
4. Configure all required properties in your flavor (`applicationId`, `versionName`, `buildConfigField`, `resValue`)
5. Enable showing the app disclaimer via `SHOW_APP_DISCLAIMER` to acknowledge its origin
6. Add a new signing config in `app/gradle.properties`
7. Customize texts for the languages which you want to offer (`values/strings.xml`, `values-de/strings.xml`, ...)
8. Add the name/s (and website/s) of the authors of the logo(s) in `copyright_logo`
9. Add track resource names in `res/xml/track_resource_names.xml`
10. Customize track colors in `res/values/colors_congress.xml`
11. Customize app colors in `res/values/colors.xml`
12. Verify colors both in light and dark mode (not all screens switch colors!)
13. Add a launcher icon in different resolutions as `res/mipmap-[...]/ic_launcher.png`
14. Add a notification icon in different resolutions as `res/drawable-[...]/ic_notification.png`
15. Add an about dialog logo as `res/drawable/dialog_logo.xml`
16. Customize bell and video recording icons in `res/drawable/` (optional)

### 3.1. Customizing illustrations shown at empty screens

The app shows illustrations on empty screens. You can customize these illustrations which fit the
look and feel of your event by importing **your own SVG files** as vector drawables to the
`res/drawable/` folder of **your product flavor**, e.g. `app/src/awesome2021/res/drawable/`.
If the same file names are used than your illustrations will replace the default ones in `main`.
The following default illustrations (vector drawables) are present in the app:

- No alarms: `app/src/main/res/drawable/no_alarms.xml`
- No favorites: `app/src/main/res/drawable/no_favorites.xml`
- No schedule: `app/src/main/res/drawable/no_schedule.xml`
- No schedule changes: `app/src/main/res/drawable/no_schedule_changes.xml`
- No search results: `app/src/main/res/drawable/no_search_results.xml`

The original raw SVG files can be found in `assets/empty-states/`, more information is available in the
associated [README](../assets/empty-states/README.md).

## 4. Optional customization

The following options can be enabled via a `buildConfigField` and configured in *app/build.gradle* as needed.

- Event postal address for easy map navigation via `EVENT_POSTAL_ADDRESS`
- Social media hashtags/handles for the event via `SOCIAL_MEDIA_HASHTAGS_HANDLES`
- Alternative schedule URL via `ENABLE_ALTERNATIVE_SCHEDULE_URL`
- c3nav integration via `C3NAV_URL`
- Chaosflix export via `ENABLE_CHAOSFLIX_EXPORT`
- Engelsystem shifts via `ENABLE_ENGELSYSTEM_SHIFTS`
  - Customize the name for the Engelsystem via `engelsystem_alias`
  - Customize the name for the Engelsystem shifts via `engelsystem_shifts_alias`
  - Customize Engelsystem shifts JSON export URL hint via `preference_hint_engelsystem_json_export_url`
- Feedback system via `SCHEDULE_FEEDBACK_URL`
- FOSDEM room status via `ENABLE_FOSDEM_ROOM_STATUS`, `FOSDEM_ROOM_STATES_URL`, `FOSDEM_ROOM_STATES_PATH`

## 5. Optional engagements

The app prompts the user for in the following topics if enabled via a `buildConfigField` in *app/build.gradle*.

- c3nav app installation via `ENGAGE_C3NAV_APP_INSTALLATION`
- Google Play beta testing via `ENGAGE_GOOGLE_BETA_TESTING`
- Google Play rating via `ENGAGE_GOOGLE_PLAY_RATING`
- to learn about the screen estate in landscape mode via `ENGAGE_LANDSCAPE_ORIENTATION`

## 6. Development features

The following features are available when the build type is "debug". They are located in the
"Development" section of the "Settings" screen. They are intended for the preparation phase of the
app to verify that the schedule data is loaded and processed correctly.

### Schedule statistic

The "Schedule statistic" screen shows the distribution of null or empty and non-empty fields in
the "sessions" database table. This can be useful for identifying missing data in the schedule.
