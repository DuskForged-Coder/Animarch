package com.example.data.api

import com.example.data.model.RecentAnimeResponse
import com.example.data.model.SeriesDetailResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnikotoApiService {

  @GET("recent-anime")
  suspend fun getRecentAnime(
    @Query("page") page: Int = 1,
    @Query("per_page") perPage: Int = 50
  ): RecentAnimeResponse

  @GET("series/{id}")
  suspend fun getSeriesDetails(
    @Path("id") id: Long
  ): SeriesDetailResponse
}
