package nerd.tuxmobil.fahrplan.congress.contract;

public interface BundleKeys {

    // Add + delete alarm
    String ALARM_SESSION_ID =
            "nerd.tuxmobil.fahrplan.congress.ALARM_SESSION_ID";
    String ALARM_DAY =
            "nerd.tuxmobil.fahrplan.congress.ALARM_DAY";
    String ALARM_TITLE =
            "nerd.tuxmobil.fahrplan.congress.ALARM_TITLE";
    String ALARM_START_TIME =
            "nerd.tuxmobil.fahrplan.congress.ALARM_START_TIME";

    // Session alarm notification
    String BUNDLE_KEY_SESSION_ALARM_SESSION_ID =
            "nerd.tuxmobil.fahrplan.congress.SESSION_ALARM_SESSION_ID";
    String BUNDLE_KEY_SESSION_ALARM_DAY_INDEX =
            "nerd.tuxmobil.fahrplan.congress.SESSION_ALARM_DAY_INDEX";

    // Session details
    String SESSION_ID =
            "nerd.tuxmobil.fahrplan.congress.SESSION_ID";

    // Side pane
    String SIDEPANE =
            "nerd.tuxmobil.fahrplan.congress.SIDEPANE";

    // Changes dialog
    String CHANGES_DLG_NUM_CHANGED =
            "nerd.tuxmobil.fahrplan.congress.ChangesDialog.NUM_CHANGES";
    String CHANGES_DLG_NUM_NEW =
            "nerd.tuxmobil.fahrplan.congress.ChangesDialog.NUM_NEW";
    String CHANGES_DLG_NUM_CANCELLED =
            "nerd.tuxmobil.fahrplan.congress.ChangesDialog.NUM_CANCELLED";
    String CHANGES_DLG_NUM_MARKED =
            "nerd.tuxmobil.fahrplan.congress.ChangesDialog.NUM_MARKED";
    String CHANGES_DLG_VERSION =
            "nerd.tuxmobil.fahrplan.congress.ChangesDialog.VERSION";

    // Shared Preferences
    String PREFS_CHANGES_SEEN =
            "nerd.tuxmobil.fahrplan.congress.Prefs.CHANGES_SEEN";
    String PREFS_SCHEDULE_LAST_FETCHED_AT =
            "nerd.tuxmobil.fahrplan.congress.Prefs.SCHEDULE_LAST_FETCHED_AT";
    String BUNDLE_KEY_SCHEDULE_URL_UPDATED =
            "nerd.tuxmobil.fahrplan.congress.Prefs.SCHEDULE_URL_UPDATED";
    String PREFS_SCHEDULE_URL =
            "nerd.tuxmobil.fahrplan.congress.Prefs.SCHEDULE_URL";
    String BUNDLE_KEY_ENGELSYSTEM_SHIFTS_URL_UPDATED =
            "nerd.tuxmobil.fahrplan.congress.Prefs.ENGELSYSTEM_SHIFTS_URL_UPDATED";
    String PREFS_ENGELSYSTEM_SHIFTS_URL =
            "nerd.tuxmobil.fahrplan.congress.Prefs.ENGELSYSTEM_SHIFTS_URL";
    String PREFS_ENGELSYSTEM_SHIFTS_HASH =
            "nerd.tuxmobil.fahrplan.congress.Prefs.ENGELSYSTEM_SHIFTS_HASH";
    String BUNDLE_KEY_ALTERNATIVE_HIGHLIGHTING_UPDATED =
            "nerd.tuxmobil.fahrplan.congress.Prefs.ALTERNATIVE_HIGHLIGHT";
    String PREFS_DISPLAY_DAY_INDEX =
            "nerd.tuxmobil.fahrplan.congress.Prefs.DISPLAY_DAY_INDEX";
}
