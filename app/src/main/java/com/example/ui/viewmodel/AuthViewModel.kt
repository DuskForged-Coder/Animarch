package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
  data object Idle : AuthUiState
  data object Loading : AuthUiState
  data class Success(val message: String) : AuthUiState
  data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
  private val authRepository: AuthRepository
) : ViewModel() {

  val currentUser: StateFlow<UserProfile?> = authRepository.currentUser

  private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  fun login(email: String, pass: String) {
    if (email.isBlank() || pass.isBlank()) {
      _uiState.value = AuthUiState.Error("Please fill in all fields.")
      return
    }

    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading
      val res = authRepository.login(email.trim(), pass.trim())
      res.fold(
        onSuccess = { user ->
          _uiState.value = AuthUiState.Success("Welcome back, ${user.displayName}!")
        },
        onFailure = { err ->
          _uiState.value = AuthUiState.Error(err.localizedMessage ?: "Login failed.")
        }
      )
    }
  }

  fun signUp(email: String, pass: String, username: String, displayName: String) {
    if (email.isBlank() || pass.isBlank() || username.isBlank()) {
      _uiState.value = AuthUiState.Error("Please fill in email, password, and username.")
      return
    }

    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading
      val res = authRepository.signUp(email.trim(), pass.trim(), username.trim(), displayName.trim())
      res.fold(
        onSuccess = { user ->
          _uiState.value = AuthUiState.Success("Account created successfully! Welcome, ${user.displayName}.")
        },
        onFailure = { err ->
          _uiState.value = AuthUiState.Error(err.localizedMessage ?: "Sign up failed.")
        }
      )
    }
  }

  fun sendPasswordReset(email: String) {
    if (email.isBlank()) {
      _uiState.value = AuthUiState.Error("Please enter your email address.")
      return
    }

    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading
      val res = authRepository.sendPasswordResetEmail(email.trim())
      res.fold(
        onSuccess = {
          _uiState.value = AuthUiState.Success("Password reset link sent to your email.")
        },
        onFailure = { err ->
          _uiState.value = AuthUiState.Error(err.localizedMessage ?: "Password reset failed.")
        }
      )
    }
  }

  fun updateProfile(displayName: String, bio: String, avatarUrl: String?, favoriteGenres: List<String>) {
    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading
      val res = authRepository.updateProfile(displayName.trim(), bio.trim(), avatarUrl, favoriteGenres)
      res.fold(
        onSuccess = {
          _uiState.value = AuthUiState.Success("Profile updated successfully!")
        },
        onFailure = { err ->
          _uiState.value = AuthUiState.Error(err.localizedMessage ?: "Profile update failed.")
        }
      )
    }
  }

  fun logout() {
    authRepository.logout()
    _uiState.value = AuthUiState.Idle
  }

  fun deleteAccount() {
    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading
      val res = authRepository.deleteAccount()
      res.fold(
        onSuccess = {
          _uiState.value = AuthUiState.Success("Account deleted.")
        },
        onFailure = { err ->
          _uiState.value = AuthUiState.Error(err.localizedMessage ?: "Failed to delete account.")
        }
      )
    }
  }

  fun clearState() {
    _uiState.value = AuthUiState.Idle
  }

  companion object {
    fun provideFactory(authRepository: AuthRepository): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return AuthViewModel(authRepository) as T
        }
      }
  }
}
