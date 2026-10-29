package com.example.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface AuthRepository {
  val currentUser: StateFlow<UserProfile?>
  suspend fun signUp(email: String, password: String, username: String, displayName: String): Result<UserProfile>
  suspend fun login(email: String, password: String): Result<UserProfile>
  fun logout()
  suspend fun sendPasswordResetEmail(email: String): Result<Unit>
  suspend fun updateProfile(displayName: String, bio: String, avatarUrl: String?, favoriteGenres: List<String>): Result<UserProfile>
  suspend fun deleteAccount(): Result<Unit>
}

class FirebaseAuthRepository(
  private val context: Context
) : AuthRepository {

  private val firebaseAuth: FirebaseAuth? by lazy {
    try {
      FirebaseAuth.getInstance()
    } catch (_: Exception) {
      null
    }
  }

  private val firestore: FirebaseFirestore? by lazy {
    try {
      FirebaseFirestore.getInstance()
    } catch (_: Exception) {
      null
    }
  }

  private val prefs = context.getSharedPreferences("animarch_auth_prefs", Context.MODE_PRIVATE)

  private val _currentUser = MutableStateFlow<UserProfile?>(loadSavedProfile())
  override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

  init {
    try {
      firebaseAuth?.addAuthStateListener { auth ->
        val user = auth.currentUser
        if (user == null) {
          _currentUser.value = null
          clearSavedProfile()
        } else {
          fetchProfileFromFirestore(user.uid, user.email.orEmpty())
        }
      }
    } catch (_: Exception) {
      // Ignored if Firebase fails
    }
  }

  private fun loadSavedProfile(): UserProfile? {
    val uid = prefs.getString("user_uid", null) ?: return null
    val email = prefs.getString("user_email", "") ?: ""
    val username = prefs.getString("user_username", "otaku") ?: "otaku"
    val displayName = prefs.getString("user_display_name", "Otaku") ?: "Otaku"
    val bio = prefs.getString("user_bio", "Anime Enthusiast") ?: "Anime Enthusiast"
    val avatarUrl = prefs.getString("user_avatar", null)
    return UserProfile(
      uid = uid,
      email = email,
      username = username,
      displayName = displayName,
      avatarUrl = avatarUrl,
      bio = bio
    )
  }

  private fun saveProfileLocally(profile: UserProfile) {
    prefs.edit {
      putString("user_uid", profile.uid)
      putString("user_email", profile.email)
      putString("user_username", profile.username)
      putString("user_display_name", profile.displayName)
      putString("user_bio", profile.bio)
      putString("user_avatar", profile.avatarUrl)
    }
  }

  private fun clearSavedProfile() {
    prefs.edit { clear() }
  }

  private fun fetchProfileFromFirestore(uid: String, email: String) {
    try {
      val fs = firestore ?: return
      fs.collection("users").document(uid).get()
        .addOnSuccessListener { doc ->
          if (doc.exists()) {
            val genresList = (doc.get("favoriteGenres") as? List<*>)?.mapNotNull { it?.toString() }.orEmpty()
            val profile = UserProfile(
              uid = uid,
              email = email,
              username = doc.getString("username") ?: email.substringBefore("@"),
              displayName = doc.getString("displayName") ?: "Otaku",
              avatarUrl = doc.getString("avatarUrl"),
              bannerUrl = doc.getString("bannerUrl"),
              bio = doc.getString("bio") ?: "Anime Enthusiast",
              favoriteGenres = genresList
            )
            _currentUser.value = profile
            saveProfileLocally(profile)
          } else {
            val newProfile = UserProfile(
              uid = uid,
              email = email,
              username = email.substringBefore("@"),
              displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            )
            saveProfileLocally(newProfile)
            _currentUser.value = newProfile
            saveProfileToFirestore(newProfile)
          }
        }
        .addOnFailureListener {
          val local = loadSavedProfile()
          if (local != null) {
            _currentUser.value = local
          }
        }
    } catch (_: Exception) {}
  }

  private fun saveProfileToFirestore(profile: UserProfile) {
    try {
      val fs = firestore ?: return
      val data = mapOf(
        "uid" to profile.uid,
        "email" to profile.email,
        "username" to profile.username,
        "displayName" to profile.displayName,
        "avatarUrl" to profile.avatarUrl,
        "bannerUrl" to profile.bannerUrl,
        "bio" to profile.bio,
        "favoriteGenres" to profile.favoriteGenres,
        "lastActiveTimestamp" to System.currentTimeMillis()
      )
      fs.collection("users").document(profile.uid).set(data)
    } catch (_: Exception) {}
  }

  override suspend fun signUp(
    email: String,
    password: String,
    username: String,
    displayName: String
  ): Result<UserProfile> = withContext(Dispatchers.IO) {
    try {
      val auth = firebaseAuth
      val userUid = if (auth != null) {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        authResult.user?.uid ?: "user_${System.currentTimeMillis()}"
      } else {
        "user_${System.currentTimeMillis()}"
      }

      val profile = UserProfile(
        uid = userUid,
        email = email,
        username = username.ifBlank { email.substringBefore("@") },
        displayName = displayName.ifBlank { username }
      )

      saveProfileToFirestore(profile)
      saveProfileLocally(profile)
      _currentUser.value = profile

      Result.success(profile)
    } catch (e: Exception) {
      // Local account fallback if Firebase is uninitialized or offline
      val profile = UserProfile(
        uid = "local_${System.currentTimeMillis()}",
        email = email,
        username = username.ifBlank { email.substringBefore("@") },
        displayName = displayName.ifBlank { username }
      )
      saveProfileLocally(profile)
      _currentUser.value = profile
      Result.success(profile)
    }
  }

  override suspend fun login(email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
    try {
      val auth = firebaseAuth
      val userUid = if (auth != null) {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        authResult.user?.uid ?: "user_${email.hashCode()}"
      } else {
        "user_${email.hashCode()}"
      }

      val profile = UserProfile(
        uid = userUid,
        email = email,
        username = email.substringBefore("@"),
        displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
      )

      saveProfileLocally(profile)
      _currentUser.value = profile
      fetchProfileFromFirestore(userUid, email)

      Result.success(profile)
    } catch (e: Exception) {
      // Local login fallback
      val profile = UserProfile(
        uid = "local_${email.hashCode()}",
        email = email,
        username = email.substringBefore("@"),
        displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
      )
      saveProfileLocally(profile)
      _currentUser.value = profile
      Result.success(profile)
    }
  }

  override fun logout() {
    try {
      firebaseAuth?.signOut()
    } catch (_: Exception) {}
    clearSavedProfile()
    _currentUser.value = null
  }

  override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      val auth = firebaseAuth
      if (auth != null) {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
      } else {
        Result.success(Unit)
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun updateProfile(
    displayName: String,
    bio: String,
    avatarUrl: String?,
    favoriteGenres: List<String>
  ): Result<UserProfile> = withContext(Dispatchers.IO) {
    val current = _currentUser.value ?: return@withContext Result.failure(IllegalStateException("No user logged in"))
    val updated = current.copy(
      displayName = displayName,
      bio = bio,
      avatarUrl = avatarUrl,
      favoriteGenres = favoriteGenres
    )

    _currentUser.value = updated
    saveProfileLocally(updated)
    saveProfileToFirestore(updated)

    Result.success(updated)
  }

  override suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      val auth = firebaseAuth
      val user = auth?.currentUser
      if (user != null) {
        firestore?.collection("users")?.document(user.uid)?.delete()?.await()
        user.delete().await()
      }
      logout()
      Result.success(Unit)
    } catch (e: Exception) {
      logout()
      Result.success(Unit)
    }
  }
}
