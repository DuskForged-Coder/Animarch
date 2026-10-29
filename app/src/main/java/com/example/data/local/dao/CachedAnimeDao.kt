package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.CachedAnimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedAnimeDao {

  @Query("SELECT * FROM cached_recent_anime ORDER BY sortOrder ASC")
  fun getAllCachedAnime(): Flow<List<CachedAnimeEntity>>

  @Query("SELECT * FROM cached_recent_anime ORDER BY sortOrder ASC")
  suspend fun getAllCachedAnimeDirect(): List<CachedAnimeEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(items: List<CachedAnimeEntity>)

  @Query("DELETE FROM cached_recent_anime")
  suspend fun clearCache()

  @Transaction
  suspend fun replaceCache(items: List<CachedAnimeEntity>) {
    clearCache()
    insertAll(items)
  }
}
