package nerd.tuxmobil.fahrplan.congress.utils

/**
 * A sealed class to encapsulate all fonts used in this project.
 */
sealed class Font(val fileName: String) {

    sealed class Roboto(fileName: String) : Font(fileName) {

        object Black : Roboto("Roboto-Black.ttf")
        object Bold : Roboto("Roboto-Bold.ttf")
        object BoldCondensed : Roboto("Roboto-BoldCondensed.ttf")
        object Light : Roboto("Roboto-Light.ttf")
        object Regular : Roboto("Roboto-Regular.ttf")

    }

}
