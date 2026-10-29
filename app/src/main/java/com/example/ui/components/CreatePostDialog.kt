package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PostCategory
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CreatePostDialog(
  onDismiss: () -> Unit,
  onSubmit: (caption: String, imageUrl: String, category: PostCategory, animeTitle: String?, isSpoiler: Boolean) -> Unit
) {
  var caption by remember { mutableStateOf("") }
  var imageUrl by remember { mutableStateOf("") }
  var animeTitle by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(PostCategory.FAN_ART) }
  var isSpoiler by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Create Community Post",
        style = MaterialTheme.typography.titleMedium.copy(
          color = TextPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Category Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(listOf(PostCategory.FAN_ART, PostCategory.EDITS, PostCategory.MEMES, PostCategory.DISCUSSIONS)) { cat ->
            val isSel = selectedCategory == cat
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSel) NeonPurple else OtakuCardSurface)
                .border(1.dp, if (isSel) NeonPurpleLight else OtakuCardBorder, RoundedCornerShape(12.dp))
                .clickable { selectedCategory = cat }
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Text(
                text = cat.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSel) Color.White else TextMuted,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp
                )
              )
            }
          }
        }

        // Caption Field
        OutlinedTextField(
          value = caption,
          onValueChange = { caption = it },
          label = { Text("Caption & Description") },
          maxLines = 3,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = OtakuCardSurface,
            unfocusedContainerColor = OtakuCardSurface,
            focusedBorderColor = NeonPurple,
            unfocusedBorderColor = OtakuCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        // Image URL Field
        OutlinedTextField(
          value = imageUrl,
          onValueChange = { imageUrl = it },
          label = { Text("Artwork / Image URL (optional)") },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = OtakuCardSurface,
            unfocusedContainerColor = OtakuCardSurface,
            focusedBorderColor = NeonPurple,
            unfocusedBorderColor = OtakuCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        // Anime Title Tag Field
        OutlinedTextField(
          value = animeTitle,
          onValueChange = { animeTitle = it },
          label = { Text("Anime Title Tag (e.g. Solo Leveling)") },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = OtakuCardSurface,
            unfocusedContainerColor = OtakuCardSurface,
            focusedBorderColor = NeonPurple,
            unfocusedBorderColor = OtakuCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        // Spoiler Alert Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Contains Spoilers?",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 13.sp)
          )
          Switch(
            checked = isSpoiler,
            onCheckedChange = { isSpoiler = it },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NeonPurple)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSubmit(caption, imageUrl, selectedCategory, animeTitle.takeIf { it.isNotBlank() }, isSpoiler)
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Publish (+20 XP)", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextMuted)
      }
    },
    containerColor = OtakuCardSurface,
    shape = RoundedCornerShape(18.dp)
  )
}
