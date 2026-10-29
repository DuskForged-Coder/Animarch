package com.example.di

import android.content.Context
import com.example.data.api.AnikotoApiService
import com.example.data.api.NetworkModule
import com.example.data.local.AnimarchDatabase
import com.example.data.repository.AnimarchAiRepository
import com.example.data.repository.AnimeRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.CommunityRepository
import com.example.data.repository.DefaultAnimarchAiRepository
import com.example.data.repository.DefaultAnimeRepository
import com.example.data.repository.DefaultCommunityRepository
import com.example.data.repository.DefaultFavoritesRepository
import com.example.data.repository.DefaultGamificationRepository
import com.example.data.repository.DefaultWatchHistoryRepository
import com.example.data.repository.FavoritesRepository
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.GamificationRepository
import com.example.data.repository.WatchHistoryRepository

interface AppContainer {
  val database: AnimarchDatabase
  val apiService: AnikotoApiService
  val animeRepository: AnimeRepository
  val watchHistoryRepository: WatchHistoryRepository
  val favoritesRepository: FavoritesRepository
  val authRepository: AuthRepository
  val gamificationRepository: GamificationRepository
  val communityRepository: CommunityRepository
  val aiRepository: AnimarchAiRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

  override val database: AnimarchDatabase by lazy {
    AnimarchDatabase.getDatabase(context)
  }

  override val apiService: AnikotoApiService by lazy {
    NetworkModule.anikotoApiService
  }

  override val animeRepository: AnimeRepository by lazy {
    DefaultAnimeRepository(
      apiService = apiService,
      aniListApiService = NetworkModule.aniListApiService,
      cachedAnimeDao = database.cachedAnimeDao(),
      favoriteEpisodeDao = database.favoriteEpisodeDao(),
      context = context
    )
  }

  override val watchHistoryRepository: WatchHistoryRepository by lazy {
    DefaultWatchHistoryRepository(database.watchHistoryDao())
  }

  override val favoritesRepository: FavoritesRepository by lazy {
    DefaultFavoritesRepository(database.favoriteEpisodeDao())
  }

  override val authRepository: AuthRepository by lazy {
    FirebaseAuthRepository(context)
  }

  override val gamificationRepository: GamificationRepository by lazy {
    DefaultGamificationRepository(context, watchHistoryRepository)
  }

  override val communityRepository: CommunityRepository by lazy {
    DefaultCommunityRepository(context, gamificationRepository)
  }

  override val aiRepository: AnimarchAiRepository by lazy {
    DefaultAnimarchAiRepository()
  }
}
