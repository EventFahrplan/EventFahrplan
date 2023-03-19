package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import info.metadude.android.eventfahrplan.database.models.Meta

interface MetaDatabaseRepository {

    fun insert(values: ContentValues): Long
    fun query(): Meta

}
