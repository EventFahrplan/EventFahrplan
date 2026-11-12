package info.metadude.android.eventfahrplan.database.extensions

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Columns.IS_FAVORITE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.models.Favorite
import org.junit.jupiter.api.Test

class FavoriteExtensionsTest {

    @Test
    fun toContentValues() {
        val favorite = Favorite(
            sessionId = "2342",
            isFavorite = true
        )
        val values = favorite.toContentValues()
        assertThat(values.getAsInteger(SESSION_ID)).isEqualTo(2342)
        assertThat(values.getAsBoolean(IS_FAVORITE)).isEqualTo(true)
    }

}
