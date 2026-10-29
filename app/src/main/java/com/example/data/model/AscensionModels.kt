package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldDub
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight

enum class MembershipRank(
  val label: String,
  val emblem: String,
  val description: String,
  val monthlyPrice: String,
  val color: Color
) {
  MORTAL("Mortal", "⚔️", "Initiate of the anime realm with full access to standard streaming.", "$0.00", Color(0xFF94A3B8)),
  CHAMPION("Champion", "🛡️", "Dedicated anime warrior with HD streaming & early releases.", "$4.99", EmeraldDub),
  IMMORTAL("Immortal", "🔮", "Master of multiple genres with custom profile frames & ad-free playback.", "$9.99", ElectricCyan),
  DEITY("Deity", "⚡", "Exalted anime sovereign with priority servers & live watch party host privileges.", "$19.99", NeonPurple),
  PRIMORDIAL("Primordial", "👑", "Transcendent origin of all realms with ultimate perks & exclusive title badge.", "$49.99", NeonPurpleLight)
}

enum class CosmeticRarity(val label: String, val color: Color) {
  COMMON("Common", Color(0xFF94A3B8)),
  RARE("Rare", ElectricCyan),
  EPIC("Epic", NeonPurpleLight),
  LEGENDARY("Legendary", Color(0xFFF59E0B))
}

enum class CosmeticCategory(val label: String) {
  FRAME("Profile Frames"),
  BANNER("Banners"),
  NAMEPLATE("Nameplates")
}

data class LevelInfo(
  val level: Int,
  val title: String,
  val currentXp: Int,
  val xpInCurrentLevel: Int,
  val xpForNextLevel: Int,
  val xpRemaining: Int,
  val progressFraction: Float
) {
  companion object {
    fun fromXp(totalXp: Int): LevelInfo {
      val level = (totalXp / 100) + 1
      val xpInCurrentLevel = totalXp % 100
      val xpForNextLevel = 100
      val xpRemaining = xpForNextLevel - xpInCurrentLevel
      val progressFraction = (xpInCurrentLevel.toFloat() / xpForNextLevel.toFloat()).coerceIn(0f, 1f)

      val title = when (level) {
        in 1..4 -> "Wanderer"
        in 5..9 -> "Seeker"
        in 10..19 -> "Awakened"
        in 20..34 -> "Ascendant"
        in 35..49 -> "Elite"
        in 50..74 -> "Mythic"
        in 75..99 -> "Transcendent"
        else -> "Eternal"
      }

      return LevelInfo(
        level = level,
        title = title,
        currentXp = totalXp,
        xpInCurrentLevel = xpInCurrentLevel,
        xpForNextLevel = xpForNextLevel,
        xpRemaining = xpRemaining,
        progressFraction = progressFraction
      )
    }
  }
}

data class AchievementItem(
  val id: String,
  val title: String,
  val description: String,
  val xpReward: Int,
  val icon: String,
  val isUnlocked: Boolean,
  val currentProgress: Int,
  val maxProgress: Int
)

data class QuestItem(
  val id: String,
  val title: String,
  val description: String,
  val xpReward: Int,
  val isCompleted: Boolean,
  val isClaimed: Boolean,
  val currentProgress: Int,
  val targetProgress: Int
)

data class CosmeticItem(
  val id: String,
  val title: String,
  val category: CosmeticCategory,
  val priceXp: Int,
  val rarity: CosmeticRarity,
  val iconEmoji: String,
  val drawableResId: Int? = null,
  val isOwned: Boolean,
  val isEquipped: Boolean
)
