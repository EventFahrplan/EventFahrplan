package nerd.tuxmobil.fahrplan.congress.changes

data class SessionChangeProperty<T>(
    val value: T,
    val contentDescription: String,
    val changeState: ChangeState,
) {

    enum class ChangeState {
        UNCHANGED,
        NEW,
        CANCELED,
        CHANGED,
    }
}
