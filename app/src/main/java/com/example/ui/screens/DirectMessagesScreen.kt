package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvatarWithFrame
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.OtakuDeepBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun DirectMessagesScreen(
  viewModel: com.example.ui.viewmodel.CommunityViewModel,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val messages by viewModel.directMessages.collectAsState()
  var messageInput by remember { mutableStateOf("") }

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
            .testTag("dm_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }
        Spacer(modifier = Modifier.width(12.dp))

        AvatarWithFrame(
          equippedFrameResId = com.example.R.drawable.frame_dragon_flame,
          size = 40.dp
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Text(
            text = "ShadowMonarch",
            style = MaterialTheme.typography.titleMedium.copy(
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 16.5.sp
            )
          )
          Text(
            text = "Active now • Community Chat",
            style = MaterialTheme.typography.labelSmall.copy(
              color = NeonPurpleLight,
              fontSize = 11.sp
            )
          )
        }
      }
    },
    bottomBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(OtakuCardSurface)
          .navigationBarsPadding()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = messageInput,
          onValueChange = { messageInput = it },
          placeholder = { Text("Write a message...", color = TextMuted) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = OtakuDeepBlack,
            unfocusedContainerColor = OtakuDeepBlack,
            focusedBorderColor = NeonPurple,
            unfocusedBorderColor = OtakuCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f)
        )

        IconButton(
          onClick = {
            if (messageInput.isNotBlank()) {
              viewModel.sendDirectMessage(messageInput)
              messageInput = ""
            }
          },
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(NeonPurple)
            .testTag("send_dm_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send Message",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    },
    containerColor = OtakuDeepBlack,
    modifier = modifier.testTag("direct_messages_screen")
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        val align = if (msg.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
        val bgColor = if (msg.isFromMe) NeonPurple else OtakuCardSurface

        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = align
        ) {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            border = if (!msg.isFromMe) androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder) else null,
            modifier = Modifier.fillMaxWidth(0.82f)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = msg.senderUsername,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (msg.isFromMe) Color.White.copy(alpha = 0.8f) else NeonPurpleLight,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = msg.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = Color.White,
                  fontSize = 13.5.sp,
                  lineHeight = 18.sp
                )
              )
            }
          }
        }
      }
    }
  }
}
