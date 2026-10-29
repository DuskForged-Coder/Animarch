package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

  private const val BASE_URL = "https://anikotoapi.site/"
  private const val ANILIST_BASE_URL = "https://graphql.anilist.co/"

  const val SUPABASE_URL = "https://bevxmwzaqlsdznqgfurp.supabase.co"
  const val SUPABASE_KEY = "sb_publishable_BzAYdolji8nxBlTZiclprQ_6XfDTeln"

  private val moshi: Moshi by lazy {
    Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()
  }

  private val headerInterceptor = Interceptor { chain ->
    val original = chain.request()
    val request = original.newBuilder()
      .header("User-Agent", "Animarch/1.0 (Linux; Android 14; Mobile; VYRON Entertainment)")
      .header("Accept", "application/json")
      .method(original.method, original.body)
      .build()
    chain.proceed(request)
  }

  private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
  }

  private val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .addInterceptor(headerInterceptor)
      .addInterceptor(loggingInterceptor)
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .build()
  }

  val anikotoApiService: AnikotoApiService by lazy {
    Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(AnikotoApiService::class.java)
  }

  val aniListApiService: AniListApiService by lazy {
    Retrofit.Builder()
      .baseUrl(ANILIST_BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(AniListApiService::class.java)
  }
}
