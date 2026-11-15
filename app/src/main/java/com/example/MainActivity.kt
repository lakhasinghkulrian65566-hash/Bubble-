package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.BoosterType
import com.example.ui.components.GameTopHud
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.SkinsScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.screens.WorldsScreen
import com.example.ui.theme.BubbleQuestTheme
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.CosmicMidnight
import com.example.ui.theme.ElectricCyan
import com.example.viewmodel.PlaySessionViewModel
import com.example.viewmodel.ProgressionViewModel
import kotlinx.coroutines.launch

private object Routes {
    const val WORLDS = "worlds"
    const val SKINS = "skins"
    const val STORE = "store"
    const val PLAY = "play/{level}/{eagle}/{doubleScore}"

    fun playRoute(level: Int, eagle: Boolean, doubleScore: Boolean): String =
        "play/$level/$eagle/$doubleScore"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            BubbleQuestTheme {
                BubbleQuestApp()
            }
        }
    }
}

@Composable
fun BubbleQuestApp(
    progressionViewModel: ProgressionViewModel = viewModel()
) {
    val navController = rememberNavController()
    val snapshot by progressionViewModel.playerSnapshot.collectAsStateWithLifecycle()
    val nextLifeSeconds by progressionViewModel.nextLifeInSeconds.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        progressionViewModel.storeFeedback.collect { feedback ->
            snackbarHostState.showSnackbar(feedback.message)
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.WORLDS
    val isInGameplay = currentRoute.startsWith("play/")

    val navItems = listOf(
        Triple(Routes.WORLDS, stringResource(R.string.nav_play), Pair(Icons.Filled.Map, Icons.Outlined.Map)),
        Triple(Routes.SKINS, stringResource(R.string.nav_skins), Pair(Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)),
        Triple(Routes.STORE, stringResource(R.string.nav_store), Pair(Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag))
    )

    fun navigateTopTab(route: String) {
        progressionViewModel.soundManager.playButtonClick()
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun launchLevel(levelNumber: Int, useEagleEye: Boolean, useDoubleScore: Boolean) {
        scope.launch {
            val eagleApplied = if (useEagleEye) progressionViewModel.consumeBoosterItem(BoosterType.EAGLE_EYE) else false
            val doubleApplied = if (useDoubleScore) progressionViewModel.consumeBoosterItem(BoosterType.DOUBLE_SCORE) else false
            progressionViewModel.soundManager.playButtonClick()
            navController.navigate(Routes.playRoute(levelNumber, eagleApplied, doubleApplied)) {
                launchSingleTop = true
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp && !isInGameplay

        Scaffold(
            containerColor = CosmicMidnight,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = if (isInGameplay) WindowInsets(0, 0, 0, 0) else WindowInsets.navigationBars,
            bottomBar = {
                if (!isInGameplay && !useRail) {
                    NavigationBar(
                        containerColor = CosmicIndigo,
                        contentColor = Color.White
                    ) {
                        navItems.forEach { (route, label, icons) ->
                            val selected = currentRoute == route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopTab(route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) icons.first else icons.second,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CosmicMidnight,
                                    selectedTextColor = ElectricCyan,
                                    indicatorColor = ElectricCyan,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("bottom_nav_$route")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = CosmicIndigo,
                        modifier = Modifier.statusBarsPadding()
                    ) {
                        navItems.forEach { (route, label, icons) ->
                            val selected = currentRoute == route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopTab(route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) icons.first else icons.second,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag("rail_nav_$route")
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    if (!isInGameplay) {
                        GameTopHud(
                            lives = snapshot.profile.lives,
                            maxLives = snapshot.profile.maxLives,
                            nextLifeInSeconds = nextLifeSeconds,
                            coins = snapshot.profile.coins,
                            totalStars = snapshot.totalStars,
                            soundEnabled = snapshot.profile.soundEnabled,
                            onToggleSound = { progressionViewModel.toggleSound() },
                            onOpenStore = { navigateTopTab(Routes.STORE) },
                            modifier = Modifier.statusBarsPadding()
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.WORLDS
                        ) {
                            composable(Routes.WORLDS) {
                                WorldsScreen(
                                    snapshot = snapshot,
                                    onLaunchLevel = ::launchLevel,
                                    onGoToStore = { navigateTopTab(Routes.STORE) },
                                    onGoToSkins = { navigateTopTab(Routes.SKINS) }
                                )
                            }
                            composable(Routes.SKINS) {
                                SkinsScreen(
                                    snapshot = snapshot,
                                    onEquipSkin = { progressionViewModel.equipSkin(it) }
                                )
                            }
                            composable(Routes.STORE) {
                                StoreScreen(
                                    snapshot = snapshot,
                                    onBuyLifeRefill = { progressionViewModel.buyLifeRefill(it) },
                                    onBuyBooster = { progressionViewModel.buyBooster(it) },
                                    onPurchaseCoinPack = { progressionViewModel.purchaseCoinPack(it) }
                                )
                            }
                            composable(
                                route = Routes.PLAY,
                                arguments = listOf(
                                    navArgument("level") { type = NavType.IntType },
                                    navArgument("eagle") { type = NavType.BoolType },
                                    navArgument("doubleScore") { type = NavType.BoolType }
                                )
                            ) { entry ->
                                val levelNum = entry.arguments?.getInt("level") ?: 1
                                val eagle = entry.arguments?.getBoolean("eagle") ?: false
                                val doubleScore = entry.arguments?.getBoolean("doubleScore") ?: false

                                val playViewModel = remember(entry.id) {
                                    PlaySessionViewModel(
                                        progressionViewModel = progressionViewModel,
                                        soundManager = progressionViewModel.soundManager
                                    )
                                }

                                LaunchedEffect(levelNum, eagle, doubleScore) {
                                    playViewModel.startLevel(levelNum, eagle, doubleScore)
                                }

                                GamePlayScreen(
                                    playViewModel = playViewModel,
                                    snapshot = snapshot,
                                    onToggleSound = { progressionViewModel.toggleSound() },
                                    onEquipSkin = { progressionViewModel.equipSkin(it) },
                                    onBuyBooster = { progressionViewModel.buyBooster(it) },
                                    onRetryLevel = { lvl ->
                                        playViewModel.startLevel(lvl)
                                    },
                                    onNextLevel = { nextLvl ->
                                        navController.navigate(Routes.playRoute(nextLvl, false, false)) {
                                            popUpTo(Routes.WORLDS)
                                        }
                                    },
                                    onGoToStore = {
                                        navController.navigate(Routes.STORE) {
                                            popUpTo(Routes.WORLDS)
                                        }
                                    },
                                    onExitToWorlds = {
                                        navController.popBackStack(Routes.WORLDS, inclusive = false)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
