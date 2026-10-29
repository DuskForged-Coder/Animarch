package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CachedAnimeDao
import com.example.data.local.dao.FavoriteEpisodeDao
import com.example.data.local.dao.WatchHistoryDao
import com.example.data.local.entity.CachedAnimeEntity
import com.example.data.local.entity.FavoriteEpisodeEntity
import com.example.data.local.entity.WatchHistoryEntity

@Database(
  entities = [
    WatchHistoryEntity::class,
    FavoriteEpisodeEntity::class,
    CachedAnimeEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AnimarchDatabase : RoomDatabase() {

  abstract fun watchHistoryDao(): WatchHistoryDao
  abstract fun favoriteEpisodeDao(): FavoriteEpisodeDao
  abstract fun cachedAnimeDao(): CachedAnimeDao

  companion object {
    @Volatile
    private var INSTANCE: AnimarchDatabase? = null

    fun getDatabase(context: Context): AnimarchDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AnimarchDatabase::class.java,
          "animarch_database"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
