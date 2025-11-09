package nerd.tuxmobil.fahrplan.congress.utils

interface Validation {
    fun validate(input: String): ValidationResult

    sealed interface ValidationResult {
        data object Success : ValidationResult
        data class Error(val errorMessage: String) : ValidationResult
    }
}
