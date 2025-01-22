package info.metadude.android.eventfahrplan.database.models

/**
 * Represents the statistic about a [column][name] in a database table.
 *
 * @param name The name of the column.
 * @param countNone The number of rows where the column value is `null` or empty.
 * @param countPresent The number of rows where the column value is not `null` nor empty.
 */
data class ColumnStatistic(
    val name: String = "",
    val countNone: Int = 0,
    val countPresent: Int = 0,
)
