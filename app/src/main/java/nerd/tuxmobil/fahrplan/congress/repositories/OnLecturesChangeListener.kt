package nerd.tuxmobil.fahrplan.congress.repositories

@Deprecated("Replace this with a push-based update mechanism")
interface OnLecturesChangeListener {
    fun onAlarmsChanged()
    fun onHighlightsChanged()
}
