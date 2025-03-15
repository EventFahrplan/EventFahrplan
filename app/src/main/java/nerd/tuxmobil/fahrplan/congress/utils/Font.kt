package nerd.tuxmobil.fahrplan.congress.utils

/**
 * A sealed class to encapsulate all fonts used in this project.
 */
sealed class Font(val fileName: String) {

    sealed class Roboto(fileName: String) : Font(fileName) {

        data object BoldCondensed : Roboto("Roboto-BoldCondensed.ttf")
        data object Light : Roboto("Roboto-Light.ttf")

    }

}
