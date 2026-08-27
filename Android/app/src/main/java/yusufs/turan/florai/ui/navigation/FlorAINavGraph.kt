package yusufs.turan.florai.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import yusufs.turan.florai.data.auth.AuthUser
import yusufs.turan.florai.domain.prediction.PredictionHistoryItem
import yusufs.turan.florai.domain.prediction.SelectedImage
import yusufs.turan.florai.ui.common.BackNavigationIcon
import yusufs.turan.florai.ui.flower.FlowerCatalogUiState
import yusufs.turan.florai.ui.history.PredictionHistoryDetailUiState
import yusufs.turan.florai.ui.history.PredictionHistoryDetailScreen
import yusufs.turan.florai.ui.history.PredictionHistoryScreen
import yusufs.turan.florai.ui.history.PredictionHistoryUiState
import yusufs.turan.florai.ui.home.HomeScreen
import yusufs.turan.florai.ui.prediction.PredictionScreen
import yusufs.turan.florai.ui.prediction.PredictionUiState
import yusufs.turan.florai.ui.profile.ProfileScreen
import yusufs.turan.florai.ui.profile.ProfileUiState
import yusufs.turan.florai.ui.settings.SettingsScreen

private data class BottomNavItem(
    val route: AppRoute,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(AppRoute.Home, "Ana", Icons.Filled.Home),
    BottomNavItem(AppRoute.Prediction, "Tahmin", Icons.Filled.CameraAlt),
    BottomNavItem(AppRoute.History, "Geçmiş", Icons.Filled.History),
    BottomNavItem(AppRoute.Settings, "Ayarlar", Icons.Filled.Settings)
)

@Composable
fun FlorAINavGraph(
    currentUser: AuthUser,
    predictionUiState: PredictionUiState,
    predictionHistoryUiState: PredictionHistoryUiState,
    predictionHistoryDetailUiState: PredictionHistoryDetailUiState,
    flowerCatalogUiState: FlowerCatalogUiState,
    profileUiState: ProfileUiState,
    onImageSelected: (SelectedImage) -> Unit,
    onPredict: () -> Unit,
    onClearImage: () -> Unit,
    onPredictionErrorShown: () -> Unit,
    onImageError: (String) -> Unit,
    onRefreshHistory: () -> Unit,
    onLoadMoreHistory: () -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onDeleteHistory: () -> Unit,
    onHistoryErrorShown: () -> Unit,
    onLoadHistoryDetail: (String, PredictionHistoryItem?) -> Unit,
    onRefreshProfile: () -> Unit,
    onSaveDisplayName: (String) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onProfileMessagesShown: () -> Unit,
    onProfileSaved: () -> Unit,
    onFlowerCatalogErrorShown: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.route.route == currentRoute }
    val appSnackbarHostState = remember { SnackbarHostState() }

    fun navigateToTopLevel(route: AppRoute) {
        navController.navigate(route.route) {
            popUpTo(AppRoute.Home.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(flowerCatalogUiState.errorMessage) {
        val message = flowerCatalogUiState.errorMessage ?: return@LaunchedEffect
        appSnackbarHostState.showSnackbar(message)
        onFlowerCatalogErrorShown()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(appSnackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                FlorAIBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = ::navigateToTopLevel
                )
            }
        }
    ) { appPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.route,
            modifier = Modifier.padding(appPadding)
        ) {
            composable(AppRoute.Home.route) {
                HomeScreen(
                    user = currentUser,
                    supportedFlowers = flowerCatalogUiState.items,
                    onOpenPrediction = { navigateToTopLevel(AppRoute.Prediction) },
                    onOpenHistory = { navigateToTopLevel(AppRoute.History) },
                    onOpenSettings = { navigateToTopLevel(AppRoute.Settings) }
                )
            }

            composable(AppRoute.Prediction.route) {
                PredictionScreen(
                    predictionUiState = predictionUiState,
                    supportedFlowers = flowerCatalogUiState.items,
                    onImageSelected = onImageSelected,
                    onPredict = onPredict,
                    onClearImage = onClearImage,
                    onPredictionErrorShown = onPredictionErrorShown,
                    onImageError = onImageError
                )
            }

            composable(AppRoute.History.route) {
                PredictionHistoryScreen(
                    uiState = predictionHistoryUiState,
                    onOpenDetails = {
                        navController.navigate(AppRoute.HistoryDetail.createRoute(it.id))
                    },
                    onRefresh = onRefreshHistory,
                    onLoadMore = onLoadMoreHistory,
                    onDeleteItem = onDeleteHistoryItem,
                    onDeleteAll = onDeleteHistory,
                    onErrorShown = onHistoryErrorShown
                )
            }

            composable(
                route = AppRoute.HistoryDetail.route,
                arguments = listOf(
                    navArgument(AppRoute.PREDICTION_ID_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val predictionId = backStackEntry.arguments
                    ?.getString(AppRoute.PREDICTION_ID_ARG)
                val cachedItem = predictionHistoryUiState.items.firstOrNull {
                    it.id == predictionId
                }

                LaunchedEffect(predictionId) {
                    if (predictionId != null) {
                        onLoadHistoryDetail(predictionId, cachedItem)
                    }
                }

                val item = predictionHistoryDetailUiState.item?.takeIf {
                    it.id == predictionId
                } ?: cachedItem

                if (item == null) {
                    MissingHistoryDetailScreen(
                        isLoading = predictionHistoryDetailUiState.isLoading,
                        errorMessage = predictionHistoryDetailUiState.errorMessage,
                        onBack = navController::navigateUp,
                        onRefresh = {
                            if (predictionId != null) {
                                onLoadHistoryDetail(predictionId, cachedItem)
                            }
                        }
                    )
                } else {
                    PredictionHistoryDetailScreen(
                        item = item,
                        onBack = navController::navigateUp
                    )
                }
            }

            composable(AppRoute.Settings.route) {
                SettingsScreen(
                    onOpenProfile = { navController.navigate(AppRoute.Profile.route) },
                    onSignOut = {
                        navigateToTopLevel(AppRoute.Home)
                        onSignOut()
                    }
                )
            }

            composable(AppRoute.Profile.route) {
                ProfileScreen(
                    uiState = profileUiState,
                    onBack = navController::navigateUp,
                    onRefresh = onRefreshProfile,
                    onSaveDisplayName = onSaveDisplayName,
                    onDeleteAccount = onDeleteAccount,
                    onMessagesShown = onProfileMessagesShown,
                    onProfileSaved = onProfileSaved
                )
            }
        }
    }
}

@Composable
private fun FlorAIBottomNavigation(
    currentRoute: String?,
    onNavigate: (AppRoute) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissingHistoryDetailScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tahmin detayı") },
                navigationIcon = { BackNavigationIcon(onBack = onBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when {
                    isLoading -> "Kayıt yükleniyor"
                    errorMessage != null -> errorMessage
                    else -> "Tahmin kaydı bulunamadı"
                },
                style = MaterialTheme.typography.titleLarge
            )
            Button(
                onClick = onRefresh,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                enabled = !isLoading
            ) {
                Text("Yenile")
            }
        }
    }
}
