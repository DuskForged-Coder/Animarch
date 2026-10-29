package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer
import com.google.firebase.FirebaseApp

class AnimarchApplication : Application() {

  lateinit var container: AppContainer
    private set

  override fun onCreate() {
    super.onCreate()
    try {
      FirebaseApp.initializeApp(this)
    } catch (_: Exception) {
      // Handled gracefully in repositories
    }
    container = DefaultAppContainer(this)
  }
}
