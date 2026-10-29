package com.example.data.api

import com.example.data.model.AniListMediaDetailResponse
import com.example.data.model.AniListQueryRequest
import com.example.data.model.AniListSearchResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AniListApiService {

  @Headers(
    "Content-Type: application/json",
    "Accept: application/json"
  )
  @POST("/")
  suspend fun searchGlobalAnime(
    @Body request: AniListQueryRequest
  ): AniListSearchResponse

  @Headers(
    "Content-Type: application/json",
    "Accept: application/json"
  )
  @POST("/")
  suspend fun getMediaDetail(
    @Body request: AniListQueryRequest
  ): AniListMediaDetailResponse
}
