package com.example.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.R
import com.example.data.model.AchievementItem
import com.example.data.model.CosmeticCategory
import com.example.data.model.CosmeticItem
import com.example.data.model.CosmeticRarity
import com.example.data.model.LevelInfo
import com.example.data.model.MembershipRank
import com.example.data.model.QuestItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface GamificationRepository {
  val totalXp: StateFlow<Int>
  val levelInfo: StateFlow<LevelInfo>
  val membershipRank: StateFlow<MembershipRank>
  val completedEpisodeIds: StateFlow<Set<Long>>
  val completedAnimeIds: StateFlow<Set<Long>>
  val watchStreakDays: StateFlow<Int>
  val quests: StateFlow<List<QuestItem>>
  val achievements: StateFlow<List<AchievementItem>>
  val cosmetics: StateFlow<List<CosmeticItem>>
  val equippedFrameResId: StateFlow<Int?>

  fun recordEpisodeCompleted(episodeId: Long, animeId: Long)
  fun recordSeriesCompleted(animeId: Long)
  fun updateMembershipRank(rank: MembershipRank)
  fun claimQuestReward(questId: String)
  fun buyOrEquipCosmetic(itemId: String): Boolean
}

class DefaultGamificationRepository(
  context: Context,
  private val watchHistoryRepository: WatchHistoryRepository
) : GamificationRepository {

  private val scope = CoroutineScope(Dispatchers.IO)
  private val prefs = context.getSharedPreferences("animarch_gamification_prefs", Context.MODE_PRIVATE)

  private val _bonusXp = MutableStateFlow(prefs.getInt("bonus_xp", 100))

  override val completedEpisodeIds: StateFlow<Set<Long>> = watchHistoryRepository.distinctEpisodeIds
    .map { it.toSet() }
    .stateIn(
      scope = scope,
      started = SharingStarted.Eagerly,
      initialValue = emptySet()
    )

  override val completedAnimeIds: StateFlow<Set<Long>> = watchHistoryRepository.distinctAnimeIds
    .map { it.toSet() }
    .stateIn(
      scope = scope,
      started = SharingStarted.Eagerly,
      initialValue = emptySet()
    )

  override val totalXp: StateFlow<Int> = combine(
    completedEpisodeIds,
    completedAnimeIds,
    _bonusXp
  ) { epSet, animeSet, bonus ->
    (epSet.size * 10) + (animeSet.size * 50) + bonus
  }.stateIn(
    scope = scope,
    started = SharingStarted.Eagerly,
    initialValue = 0
  )

  override val levelInfo: StateFlow<LevelInfo> = totalXp
    .map { xp -> LevelInfo.fromXp(xp) }
    .stateIn(
      scope = scope,
      started = SharingStarted.Eagerly,
      initialValue = LevelInfo.fromXp(0)
    )

  private val _membershipRank = MutableStateFlow(loadRank())
  override val membershipRank: StateFlow<MembershipRank> = _membershipRank.asStateFlow()

  override val watchStreakDays: StateFlow<Int> = watchHistoryRepository.watchTimestamps
    .map { timestamps -> calculateStreak(timestamps) }
    .stateIn(
      scope = scope,
      started = SharingStarted.Eagerly,
      initialValue = 0
    )

  private val _quests = MutableStateFlow(loadQuests())
  override val quests: StateFlow<List<QuestItem>> = _quests.asStateFlow()

  private val _achievements = MutableStateFlow(loadAchievements())
  override val achievements: StateFlow<List<AchievementItem>> = _achievements.asStateFlow()

  private val _cosmetics = MutableStateFlow(loadCosmetics())
  override val cosmetics: StateFlow<List<CosmeticItem>> = _cosmetics.asStateFlow()

  private val _equippedFrameResId = MutableStateFlow<Int?>(loadEquippedFrameResId())
  override val equippedFrameResId: StateFlow<Int?> = _equippedFrameResId.asStateFlow()

  private fun loadRank(): MembershipRank {
    val rankName = prefs.getString("membership_rank", MembershipRank.MORTAL.name) ?: MembershipRank.MORTAL.name
    return try { MembershipRank.valueOf(rankName) } catch (_: Exception) { MembershipRank.MORTAL }
  }

  private fun loadEquippedFrameResId(): Int? {
    val frameId = prefs.getString("equipped_frame_id", "frame_void_prism") ?: return null
    return when (frameId) {
      "frame_void_prism" -> R.drawable.frame_void_prism
      "frame_dragon_flame" -> R.drawable.frame_dragon_flame
      "frame_celestial_crystal" -> R.drawable.frame_celestial_crystal
      else -> null
    }
  }

  private fun addBonusXp(amount: Int) {
    val newBonus = _bonusXp.value + amount
    _bonusXp.value = newBonus
    prefs.edit { putInt("bonus_xp", newBonus) }
  }

  private fun calculateStreak(timestamps: List<Long>): Int {
    if (timestamps.isEmpty()) return 0
    val millisPerDay = 86400000L
    val now = System.currentTimeMillis()
    val daysSinceEpoch = timestamps.map { it / millisPerDay }.distinct().sortedDescending()
    val todayEpoch = now / millisPerDay

    var streak = 0
    var currentDay = todayEpoch
    for (day in daysSinceEpoch) {
      if (day == currentDay || day == currentDay - 1) {
        streak++
        currentDay = day - 1
      } else {
        break
      }
    }
    return streak.coerceAtLeast(1)
  }

  override fun recordEpisodeCompleted(episodeId: Long, animeId: Long) {
    updateQuestProgress("daily_ep_1", completedEpisodeIds.value.size + 1)
  }

  override fun recordSeriesCompleted(animeId: Long) {}

  override fun updateMembershipRank(rank: MembershipRank) {
    _membershipRank.value = rank
    prefs.edit { putString("membership_rank", rank.name) }
  }

  private fun updateQuestProgress(questId: String, currentProgress: Int) {
    val updated = _quests.value.map { quest ->
      if (quest.id == questId && !quest.isCompleted) {
        val newProgress = currentProgress.coerceAtMost(quest.targetProgress)
        quest.copy(
          currentProgress = newProgress,
          isCompleted = newProgress >= quest.targetProgress
        )
      } else quest
    }
    _quests.value = updated
  }

  override fun claimQuestReward(questId: String) {
    val list = _quests.value.toMutableList()
    val idx = list.indexOfFirst { it.id == questId }
    if (idx != -1) {
      val quest = list[idx]
      if (quest.isCompleted && !quest.isClaimed) {
        addBonusXp(quest.xpReward)
        list[idx] = quest.copy(isClaimed = true)
        _quests.value = list
      }
    }
  }

  override fun buyOrEquipCosmetic(itemId: String): Boolean {
    val list = _cosmetics.value.toMutableList()
    val idx = list.indexOfFirst { it.id == itemId }
    if (idx == -1) return false

    val item = list[idx]
    if (!item.isOwned) {
      if (totalXp.value >= item.priceXp) {
        // Purchase & equip
        val updated = list.map { c ->
          if (c.category == item.category) c.copy(isEquipped = c.id == item.id, isOwned = if (c.id == item.id) true else c.isOwned)
          else c
        }
        _cosmetics.value = updated
        _equippedFrameResId.value = item.drawableResId
        prefs.edit { putString("equipped_frame_id", item.id) }
        return true
      }
      return false
    } else {
      // Toggle equip state
      val willEquip = !item.isEquipped
      val updated = list.map { c ->
        if (c.category == item.category) {
          if (c.id == item.id) c.copy(isEquipped = willEquip)
          else c.copy(isEquipped = false)
        } else c
      }
      _cosmetics.value = updated
      val newResId = if (willEquip) item.drawableResId else null
      _equippedFrameResId.value = newResId
      prefs.edit { putString("equipped_frame_id", if (willEquip) item.id else "") }
      return true
    }
  }

  private fun loadQuests(): List<QuestItem> {
    return listOf(
      QuestItem(
        id = "daily_ep_1",
        title = "Complete One Episode",
        description = "Watch and complete 1 full episode of any series",
        xpReward = 10,
        isCompleted = false,
        isClaimed = false,
        currentProgress = 0,
        targetProgress = 1
      ),
      QuestItem(
        id = "daily_explore_3",
        title = "Genre Explorer",
        description = "Explore or save anime from 3 different genres",
        xpReward = 20,
        isCompleted = true,
        isClaimed = false,
        currentProgress = 3,
        targetProgress = 3
      ),
      QuestItem(
        id = "weekly_streak_7",
        title = "Consistent Soul",
        description = "Maintain a 7-day watch streak",
        xpReward = 50,
        isCompleted = true,
        isClaimed = true,
        currentProgress = 7,
        targetProgress = 7
      )
    )
  }

  private fun loadAchievements(): List<AchievementItem> {
    val currentEpisodes = completedEpisodeIds.value.size
    val currentAnime = completedAnimeIds.value.size
    return listOf(
      AchievementItem("first_steps", "First Steps", "Complete your profile setup", 100, "🌟", true, 1, 1),
      AchievementItem("first_ep", "First Episode", "Complete your first episode", 10, "▶️", currentEpisodes >= 1, currentEpisodes.coerceAtMost(1), 1),
      AchievementItem("ep_10", "Marathon Initiate", "Complete 10 unique episodes", 50, "🔥", currentEpisodes >= 10, currentEpisodes.coerceAtMost(10), 10),
      AchievementItem("ep_50", "Marathon Master", "Complete 50 unique episodes", 150, "⚡", currentEpisodes >= 50, currentEpisodes.coerceAtMost(50), 50),
      AchievementItem("series_5", "Series Conqueror", "Complete 5 full anime series", 200, "🏆", currentAnime >= 5, currentAnime.coerceAtMost(5), 5),
      AchievementItem("streak_7", "Consistent Soul", "Maintain a 7-day watch streak", 50, "🛡️", watchStreakDays.value >= 7, watchStreakDays.value.coerceAtMost(7), 7)
    )
  }

  private fun loadCosmetics(): List<CosmeticItem> {
    val equippedId = prefs.getString("equipped_frame_id", "frame_void_prism")
    return listOf(
      CosmeticItem("frame_void_prism", "Void Prism", CosmeticCategory.FRAME, 500, CosmeticRarity.EPIC, "🔮", R.drawable.frame_void_prism, true, equippedId == "frame_void_prism"),
      CosmeticItem("frame_dragon_flame", "Dragon Flame", CosmeticCategory.FRAME, 1200, CosmeticRarity.LEGENDARY, "🐉", R.drawable.frame_dragon_flame, true, equippedId == "frame_dragon_flame"),
      CosmeticItem("frame_celestial_crystal", "Celestial Crystal", CosmeticCategory.FRAME, 800, CosmeticRarity.RARE, "💎", R.drawable.frame_celestial_crystal, true, equippedId == "frame_celestial_crystal")
    )
  }
}
