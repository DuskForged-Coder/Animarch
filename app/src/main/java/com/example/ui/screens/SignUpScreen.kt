package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.OtakuDeepBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
  viewModel: AuthViewModel,
  onBackClick: () -> Unit,
  onNavigateToLogin: () -> Unit,
  onSignUpSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val uiState by viewModel.uiState.collectAsState()

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var username by remember { mutableStateOf("") }
  var displayName by remember { mutableStateOf("") }

  LaunchedEffect(currentUser) {
    if (currentUser != null) {
      onSignUpSuccess()
    }
  }

  Scaffold(
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(OtakuDeepBlack)
          .statusBarsPadding()
          .displayCutoutPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(OtakuCardSurface)
            .testTag("signup_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "Create Account",
          style = MaterialTheme.typography.titleMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        )
      }
    },
    containerColor = OtakuDeepBlack,
    modifier = modifier.testTag("signup_screen")
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .navigationBarsPadding()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = "Join ANIMARCH",
        style = MaterialTheme.typography.headlineLarge.copy(
          color = TextPrimary,
          fontWeight = FontWeight.Black,
          fontSize = 26.sp
        )
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Create an account to save watch progress and sync lists",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = TextMuted,
          fontSize = 13.sp
        )
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Email field
      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email Address") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonPurpleLight) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = OtakuCardSurface,
          unfocusedContainerColor = OtakuCardSurface,
          focusedBorderColor = NeonPurple,
          unfocusedBorderColor = OtakuCardBorder,
          focusedLabelColor = NeonPurpleLight,
          unfocusedLabelColor = TextMuted,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Username field
      OutlinedTextField(
        value = username,
        onValueChange = { username = it },
        label = { Text("Username") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonPurpleLight) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = OtakuCardSurface,
          unfocusedContainerColor = OtakuCardSurface,
          focusedBorderColor = NeonPurple,
          unfocusedBorderColor = OtakuCardBorder,
          focusedLabelColor = NeonPurpleLight,
          unfocusedLabelColor = TextMuted,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Display Name field
      OutlinedTextField(
        value = displayName,
        onValueChange = { displayName = it },
        label = { Text("Display Name") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonPurpleLight) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = OtakuCardSurface,
          unfocusedContainerColor = OtakuCardSurface,
          focusedBorderColor = NeonPurple,
          unfocusedBorderColor = OtakuCardBorder,
          focusedLabelColor = NeonPurpleLight,
          unfocusedLabelColor = TextMuted,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Password field
      OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonPurpleLight) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = OtakuCardSurface,
          unfocusedContainerColor = OtakuCardSurface,
          focusedBorderColor = NeonPurple,
          unfocusedBorderColor = OtakuCardBorder,
          focusedLabelColor = NeonPurpleLight,
          unfocusedLabelColor = TextMuted,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Status messages
      when (val state = uiState) {
        is AuthUiState.Error -> {
          Text(
            text = state.message,
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.error,
              fontSize = 12.5.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp)
          )
        }
        is AuthUiState.Success -> {
          Text(
            text = state.message,
            style = MaterialTheme.typography.bodySmall.copy(
              color = NeonPurpleLight,
              fontSize = 12.5.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp)
          )
        }
        else -> {}
      }

      // Submit Button
      Button(
        onClick = { viewModel.signUp(email, password, username, displayName) },
        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
        shape = RoundedCornerShape(12.dp),
        enabled = uiState !is AuthUiState.Loading,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("signup_submit_button")
      ) {
        if (uiState is AuthUiState.Loading) {
          CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(22.dp))
        } else {
          Text(
            text = "Create Account",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Already have an account? ",
          style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 13.sp)
        )
        Text(
          text = "Sign In",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = NeonPurpleLight,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          ),
          modifier = Modifier.clickable { onNavigateToLogin() }
        )
      }
    }
  }
}
