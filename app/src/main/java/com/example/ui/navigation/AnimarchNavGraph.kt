package com.example.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.di.AppContainer
import com.example.ui.components.FloatingBottomNavBar
import com.example.ui.components.LiquidGlassAiChatSheet
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.DirectMessagesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.OtakuDeepBlack
import com.example.ui.viewmodel.AnimarchAiViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CommunityViewModel
import com.example.ui.viewmodel.DetailViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.PlayerViewModel

sealed class Screen(val route: String) {
  data object Splash : Screen("splash")
  data object Home : Screen("home")
  data object Community : Screen("community")
  data object DirectMessages : Screen("direct_messages")
  data object Profile : Screen("profile")
  data object Shop : Screen("shop")
  data object Login : Screen("login")
  data object SignUp : Screen("signup")
  data object Detail : Screen("detail/{animeId}") {
    fun createRoute(animeId: Long): String = "detail/$animeId"
  }
  data object Player : Screen("player/{animeId}/{seriesTitle}/{episodeId}") {
    fun createRoute(animeId: Long, seriesTitle: String, episodeId: Long): String {
      val encodedTitle = Uri.encode(seriesTitle)
      return "player/$animeId/$encodedTitle/$episodeId"
    }
  }
}

@Composable
fun AnimarchNavGraph(
  appContainer: AppContainer,
  isInPiPMode: Boolean = false,
  onEnterPiP: () -> Unit = {},
  onPlayerActiveChange: (Boolean) -> Unit = {},
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController()
) {
  val authViewModel: AuthViewModel = viewModel(
    factory = AuthViewModel.provideFactory(appContainer.authRepository)
  )

  val communityViewModel: CommunityViewModel = viewModel(
    factory = CommunityViewModel.provideFactory(appContainer.communityRepository)
  )

  val aiViewModel: AnimarchAiViewModel = viewModel(
    factory = AnimarchAiViewModel.provideFactory(
      aiRepository = appContainer.aiRepository,
      animeRepository = appContainer.animeRepository
    )
  )

  var showAiSheet by remember { mutableStateOf(false) }

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val showBottomBar = !isInPiPMode && currentRoute in listOf(
    Screen.Home.route,
    Screen.Community.route,
    Screen.Profile.route,
    Screen.Shop.route
  )

  Scaffold(
    containerColor = OtakuDeepBlack,
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.fillMaxSize()
      ) {
        composable(Screen.Splash.route) {
          SplashScreen(
            onNavigateToHome = {
              navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
              }
            }
          )
        }

        composable(Screen.Home.route) {
          val homeViewModel: HomeViewModel = viewModel(
            factory = HomeViewModel.provideFactory(
              repository = appContainer.animeRepository,
              watchHistoryRepository = appContainer.watchHistoryRepository
            )
          )
          HomeScreen(
            viewModel = homeViewModel,
            gamificationRepository = appContainer.gamificationRepository,
            onAnimeClick = { animeId ->
              navController.navigate(Screen.Detail.createRoute(animeId))
            },
            onEpisodeClick = { animeId, seriesTitle, episodeId ->
              navController.navigate(Screen.Player.createRoute(animeId, seriesTitle, episodeId))
            },
            onProfileClick = {
              val user = authViewModel.currentUser.value
              if (user != null) {
                navController.navigate(Screen.Profile.route)
              } else {
                navController.navigate(Screen.Login.route)
              }
            },
            onAiClick = { showAiSheet = true }
          )
        }

        composable(Screen.Community.route) {
          CommunityScreen(
            viewModel = communityViewModel,
            onNavigateToDirectMessages = {
              navController.navigate(Screen.DirectMessages.route)
            }
          )
        }

        composable(Screen.DirectMessages.route) {
          DirectMessagesScreen(
            viewModel = communityViewModel,
            onBackClick = { navController.popBackStack() }
          )
        }

        composable(Screen.Profile.route) {
          ProfileScreen(
            viewModel = authViewModel,
            gamificationRepository = appContainer.gamificationRepository,
            onBackClick = { navController.popBackStack() },
            onLoginClick = { navController.navigate(Screen.Login.route) }
          )
        }

        composable(Screen.Shop.route) {
          ShopScreen(
            gamificationRepository = appContainer.gamificationRepository
          )
        }

        composable(Screen.Login.route) {
          LoginScreen(
            viewModel = authViewModel,
            onBackClick = { navController.popBackStack() },
            onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
            onLoginSuccess = {
              navController.navigate(Screen.Profile.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
              }
            }
          )
        }

        composable(Screen.SignUp.route) {
          SignUpScreen(
            viewModel = authViewModel,
            onBackClick = { navController.popBackStack() },
            onNavigateToLogin = { navController.popBackStack() },
            onSignUpSuccess = {
              navController.navigate(Screen.Profile.route) {
                popUpTo(Screen.SignUp.route) { inclusive = true }
              }
            }
          )
        }

        composable(
          route = Screen.Detail.route,
          arguments = listOf(
            navArgument("animeId") { type = NavType.LongType }
          )
        ) { backStackEntry ->
          val animeId = backStackEntry.arguments?.getLong("animeId") ?: 0L
          val detailViewModel: DetailViewModel = viewModel(
            key = "detail_$animeId",
            factory = DetailViewModel.provideFactory(
              animeId = animeId,
              repository = appContainer.animeRepository,
              favoritesRepository = appContainer.favoritesRepository
            )
          )
          DetailScreen(
            viewModel = detailViewModel,
            onBackClick = { navController.popBackStack() },
            onAnimeClick = { targetAnimeId ->
              navController.navigate(Screen.Detail.createRoute(targetAnimeId))
            },
            onEpisodeClick = { id, title, episodeId ->
              navController.navigate(Screen.Player.createRoute(id, title, episodeId))
            }
          )
        }

        composable(
          route = Screen.Player.route,
          arguments = listOf(
            navArgument("animeId") { type = NavType.LongType },
            navArgument("seriesTitle") { type = NavType.StringType },
            navArgument("episodeId") { type = NavType.LongType }
          )
        ) { backStackEntry ->
          val animeId = backStackEntry.arguments?.getLong("animeId") ?: 0L
          val rawTitle = backStackEntry.arguments?.getString("seriesTitle") ?: "Anime"
          val decodedTitle = Uri.decode(rawTitle)
          val episodeId = backStackEntry.arguments?.getLong("episodeId") ?: 0L

          val playerViewModel: PlayerViewModel = viewModel(
            key = "player_${animeId}_$episodeId",
            factory = PlayerViewModel.provideFactory(
              animeId = animeId,
              initialTitle = decodedTitle,
              initialEpisodeId = episodeId,
              repository = appContainer.animeRepository,
              watchHistoryRepository = appContainer.watchHistoryRepository,
              favoritesRepository = appContainer.favoritesRepository,
              gamificationRepository = appContainer.gamificationRepository
            )
          )

          PlayerScreen(
            viewModel = playerViewModel,
            onBackClick = { navController.popBackStack() },
            onAnimeClick = { targetAnimeId ->
              navController.navigate(Screen.Detail.createRoute(targetAnimeId))
            },
            isInPiPMode = isInPiPMode,
            onEnterPiP = onEnterPiP,
            onPlayerActiveChange = onPlayerActiveChange
          )
        }
      }

      if (showBottomBar) {
        FloatingBottomNavBar(
          currentRoute = currentRoute,
          onNavigate = { destination ->
            if (currentRoute != destination.route) {
              navController.navigate(destination.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            }
          },
          modifier = Modifier.align(Alignment.BottomCenter)
        )
      }

      // Liquid Glass AI Recommendation Chat Assistant Sheet
      if (showAiSheet) {
        LiquidGlassAiChatSheet(
          viewModel = aiViewModel,
          onDismiss = { showAiSheet = false },
          onAnimeClick = { animeId ->
            showAiSheet = false
            navController.navigate(Screen.Detail.createRoute(animeId))
          }
        )
      }
    }
  }
}
