package com.example.data.model

data class UserProfile(
  val uid: String = "",
  val email: String = "",
  val username: String = "",
  val displayName: String = "",
  val avatarUrl: String? = null,
  val bannerUrl: String? = null,
  val bio: String = "Anime Enthusiast",
  val favoriteGenres: List<String> = emptyList(),
  val isOnline: Boolean = true,
  val lastActiveTimestamp: Long = System.currentTimeMillis()
)
