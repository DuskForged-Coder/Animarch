package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FavoriteEpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteEpisodeDao {

  @Query("SELECT * FROM favorite_episodes ORDER BY bookmarkedTimestamp DESC")
  fun getAllFavorites(): Flow<List<FavoriteEpisodeEntity>>

  @Query("SELECT EXISTS(SELECT 1 FROM favorite_episodes WHERE episodeId = :episodeId)")
  fun isEpisodeFavorited(episodeId: Long): Flow<Boolean>

  @Query("SELECT EXISTS(SELECT 1 FROM favorite_episodes WHERE episodeId = :episodeId)")
  suspend fun isFavoriteDirect(episodeId: Long): Boolean

  @Query("SELECT DISTINCT animeId FROM favorite_episodes")
  fun getFavoriteAnimeIds(): Flow<List<Long>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFavorite(favorite: FavoriteEpisodeEntity)

  @Query("DELETE FROM favorite_episodes WHERE episodeId = :episodeId")
  suspend fun deleteFavorite(episodeId: Long)

  @Query("DELETE FROM favorite_episodes")
  suspend fun clearAllFavorites()
}
