package com.soniq.music.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soniq.music.database.SoniqDatabaseContract
import com.soniq.music.database.entity.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {

    @Query("SELECT * FROM ${SoniqDatabaseContract.Tables.RECENT_SEARCHES} ORDER BY searched_at_epoch_ms DESC LIMIT 10")
    fun observeRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: RecentSearchEntity)

    @Query("DELETE FROM ${SoniqDatabaseContract.Tables.RECENT_SEARCHES}")
    suspend fun clearAll()
}
