package com.mm.astraisandroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mm.astraisandroid.data.local.entities.GrupoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrupoDao {
    @Query("SELECT * FROM grupos ORDER BY name ASC")
    fun getAllGroups(): Flow<List<GrupoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GrupoEntity>)

    @Query("DELETE FROM grupos WHERE id = :gid")
    suspend fun deleteGroupById(gid: Int)

    @Query("DELETE FROM grupos")
    suspend fun clearAll()
}
