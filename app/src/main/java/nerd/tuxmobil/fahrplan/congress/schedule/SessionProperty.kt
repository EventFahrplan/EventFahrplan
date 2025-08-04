package nerd.tuxmobil.fahrplan.congress.schedule

data class SessionProperty<T>(
    val value: T,
    val contentDescription: String,
    val maxLines: Int = Int.MAX_VALUE,
)
