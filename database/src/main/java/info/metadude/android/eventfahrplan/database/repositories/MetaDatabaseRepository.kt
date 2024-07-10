package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.content.Context
import info.metadude.android.eventfahrplan.database.models.Meta
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.MetaDBOpenHelper

interface MetaDatabaseRepository {

    companion object {
        fun get(context: Context): MetaDatabaseRepository =
            RealMetaDatabaseRepository(MetaDBOpenHelper(context))
    }

    fun insert(values: ContentValues): Long
    fun query(): Meta

}
