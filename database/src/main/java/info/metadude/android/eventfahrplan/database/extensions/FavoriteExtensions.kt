package info.metadude.android.eventfahrplan.database.extensions

import androidx.core.content.contentValuesOf
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Columns.IS_FAVORITE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Values.MARKED_AS_FAVORITE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Values.NOT_MARKED_AS_FAVORITE
import info.metadude.android.eventfahrplan.database.models.Favorite

fun Favorite.toContentValues() = contentValuesOf(
        SESSION_ID to sessionId,
        IS_FAVORITE to if (isFavorite) MARKED_AS_FAVORITE else NOT_MARKED_AS_FAVORITE
)
