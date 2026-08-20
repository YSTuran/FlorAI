package yusufs.turan.florai.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import yusufs.turan.florai.data.auth.AuthUser
import yusufs.turan.florai.domain.prediction.PredictionHistoryItem
import yusufs.turan.florai.domain.prediction.SelectedImage
import yusufs.turan.florai.ui.common.BackNavigationIcon
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

@Composable
fun FlorAINavGraph(
    currentUser: AuthUser,
    predictionUiState: PredictionUiState,
    predictionHistoryUiState: PredictionHistoryUiState,
    predictionHistoryDetailUiState: PredictionHistoryDetailUiState,
    profileUiState: ProfileUiState,
    onImageSelected: (SelectedImage) -> Unit,
    onPredict: () -> Unit,
    onClearImage: () -> Unit,
    onPredictionErrorShown: () -> Unit,
    onImageError: (String) -> Unit,
    onRefreshHistory: () -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onDeleteHistory: () -> Unit,
    onHistoryErrorShown: () -> Unit,
    onLoadHistoryDetail: (String, PredictionHistoryItem?) -> Unit,
    onRefreshProfile: () -> Unit,
    onSaveDisplayName: (String) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onProfileMessagesShown: () -> Unit,
    onProfileSaved: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
        modifier = modifier
    ) {
        composable(AppRoute.Home.route) {
            HomeScreen(
                user = currentUser,
                onOpenPrediction = { navController.navigate(AppRoute.Prediction.route) },
                onOpenHistory = { navController.navigate(AppRoute.History.route) },
                onOpenSettings = { navController.navigate(AppRoute.Settings.route) }
            )
        }

        composable(AppRoute.Prediction.route) {
            PredictionScreen(
                predictionUiState = predictionUiState,
                onImageSelected = onImageSelected,
                onPredict = onPredict,
                onClearImage = onClearImage,
                onPredictionErrorShown = onPredictionErrorShown,
                onImageError = onImageError,
                onBack = navController::navigateUp
            )
        }

        composable(AppRoute.History.route) {
            PredictionHistoryScreen(
                uiState = predictionHistoryUiState,
                onBack = navController::navigateUp,
                onOpenDetails = {
                    navController.navigate(AppRoute.HistoryDetail.createRoute(it.id))
                },
                onRefresh = onRefreshHistory,
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
                onBack = navController::navigateUp,
                onOpenProfile = { navController.navigate(AppRoute.Profile.route) },
                onSignOut = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
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
                title = { Text("Tahmin detayi") },
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
                    isLoading -> "Kayit yukleniyor"
                    errorMessage != null -> errorMessage
                    else -> "Tahmin kaydi bulunamadi"
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
