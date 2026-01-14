package nerd.tuxmobil.fahrplan.congress.commons

import nerd.tuxmobil.fahrplan.congress.BuildConfig

data class BuildConfigProvider(
    override val packageName: String = BuildConfig.APPLICATION_ID,
    override val versionName: String = BuildConfig.VERSION_NAME,
    override val versionCode: Int = BuildConfig.VERSION_CODE,
    override val eventPostalAddress: String = BuildConfig.EVENT_POSTAL_ADDRESS,
    override val eventWebsiteUrl: String = BuildConfig.EVENT_WEBSITE_URL,
    override val showAppDisclaimer: Boolean = BuildConfig.SHOW_APP_DISCLAIMER,
    override val translationPlatformUrl: String = BuildConfig.TRANSLATION_PLATFORM_URL,
    override val sourceCodeUrl: String = BuildConfig.SOURCE_CODE_URL,
    override val issuesUrl: String = BuildConfig.ISSUES_URL,
    override val fDroidUrl: String = BuildConfig.F_DROID_URL,
    override val googlePlayUrl: String = BuildConfig.GOOGLE_PLAY_URL,
    override val dataPrivacyStatementDeUrl: String = BuildConfig.DATA_PRIVACY_STATEMENT_DE_URL,
    override val enableFosdemRoomStates: Boolean = BuildConfig.ENABLE_FOSDEM_ROOM_STATES,
    override val fosdemRoomStatesPath: String = BuildConfig.FOSDEM_ROOM_STATES_PATH,
    override val fosdemRoomStatesUrl: String = BuildConfig.FOSDEM_ROOM_STATES_URL,
    override val scheduleUrl: String = BuildConfig.SCHEDULE_URL,
    override val serverBackendType: String = BuildConfig.SERVER_BACKEND_TYPE,
    override val enableEngelsystemShifts: Boolean = BuildConfig.ENABLE_ENGELSYSTEM_SHIFTS,
) : BuildConfigProvision
