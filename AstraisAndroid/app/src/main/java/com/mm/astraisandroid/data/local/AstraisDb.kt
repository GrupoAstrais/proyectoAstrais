package com.mm.astraisandroid.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mm.astraisandroid.data.local.dao.ActionDao
import com.mm.astraisandroid.data.local.dao.TareaDao
import com.mm.astraisandroid.data.local.entities.PendingAction
import com.mm.astraisandroid.data.local.entities.TareaEntity

@Database(entities = [TareaEntity::class, PendingAction::class], version = 1, exportSchema = false)
abstract class AstraisDb : RoomDatabase() {
    abstract fun tareaDao(): TareaDao
    abstract fun actionDao(): ActionDao

    companion object {
        @Volatile
        private var INSTANCE: AstraisDb? = null

        fun getInstance(context: Context): AstraisDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstraisDb::class.java,
                    "astrais.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}