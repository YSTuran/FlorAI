package yusufs.turan.florai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import yusufs.turan.florai.domain.prediction.PredictionHistoryItem
import yusufs.turan.florai.ui.auth.AuthScreen
import yusufs.turan.florai.ui.auth.AuthViewModel
import yusufs.turan.florai.ui.history.PredictionHistoryDetailScreen
import yusufs.turan.florai.ui.history.PredictionHistoryScreen
import yusufs.turan.florai.ui.history.PredictionHistoryViewModel
import yusufs.turan.florai.ui.home.HomeScreen
import yusufs.turan.florai.ui.prediction.PredictionScreen
import yusufs.turan.florai.ui.prediction.PredictionViewModel
import yusufs.turan.florai.ui.profile.ProfileScreen
import yusufs.turan.florai.ui.profile.ProfileViewModel
import yusufs.turan.florai.ui.settings.SettingsScreen

@Composable
fun FlorAIApp(
    authViewModel: AuthViewModel = viewModel(),
    predictionViewModel: PredictionViewModel = viewModel(),
    predictionHistoryViewModel: PredictionHistoryViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val predictionUiState by predictionViewModel.uiState.collectAsState()
    val predictionHistoryUiState by predictionHistoryViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser
    var appDestination by rememberSaveable { mutableStateOf(AppDestination.Home) }
    var selectedHistoryItem by remember { mutableStateOf<PredictionHistoryItem?>(null) }

    if (currentUser == null || !currentUser.emailVerified) {
        AuthScreen(
            uiState = uiState,
            onSignIn = authViewModel::signIn,
            onRegister = authViewModel::register,
            onForgotPassword = authViewModel::sendPasswordResetEmail,
            onResendVerification = authViewModel::resendEmailVerification,
            onRefreshVerification = authViewModel::refreshEmailVerification,
            onSignOut = authViewModel::signOut,
            onMessagesShown = authViewModel::clearMessages
        )
    } else {
        when (appDestination) {
            AppDestination.Home -> {
                HomeScreen(
                    user = currentUser,
                    onOpenPrediction = { appDestination = AppDestination.Prediction },
                    onOpenHistory = { appDestination = AppDestination.History },
                    onOpenSettings = { appDestination = AppDestination.Settings }
                )
            }

            AppDestination.Prediction -> {
                PredictionScreen(
                    predictionUiState = predictionUiState,
                    onImageSelected = predictionViewModel::setSelectedImage,
                    onPredict = predictionViewModel::predict,
                    onClearImage = predictionViewModel::clearSelectedImage,
                    onPredictionErrorShown = predictionViewModel::clearError,
                    onImageError = predictionViewModel::showError,
                    onBack = { appDestination = AppDestination.Home }
                )
            }

            AppDestination.History -> {
                PredictionHistoryScreen(
                    uiState = predictionHistoryUiState,
                    onBack = { appDestination = AppDestination.Home },
                    onOpenDetails = {
                        selectedHistoryItem = it
                        appDestination = AppDestination.HistoryDetail
                    },
                    onRefresh = predictionHistoryViewModel::loadHistory,
                    onDeleteItem = predictionHistoryViewModel::deleteItem,
                    onDeleteAll = predictionHistoryViewModel::deleteAll,
                    onErrorShown = predictionHistoryViewModel::clearError
                )
            }

            AppDestination.HistoryDetail -> {
                selectedHistoryItem?.let { item ->
                    PredictionHistoryDetailScreen(
                        item = item,
                        onBack = { appDestination = AppDestination.History }
                    )
                } ?: PredictionHistoryScreen(
                    uiState = predictionHistoryUiState,
                    onBack = { appDestination = AppDestination.Home },
                    onOpenDetails = {
                        selectedHistoryItem = it
                        appDestination = AppDestination.HistoryDetail
                    },
                    onRefresh = predictionHistoryViewModel::loadHistory,
                    onDeleteItem = predictionHistoryViewModel::deleteItem,
                    onDeleteAll = predictionHistoryViewModel::deleteAll,
                    onErrorShown = predictionHistoryViewModel::clearError
                )
            }

            AppDestination.Settings -> {
                SettingsScreen(
                    onBack = { appDestination = AppDestination.Home },
                    onOpenProfile = { appDestination = AppDestination.Profile },
                    onSignOut = {
                        appDestination = AppDestination.Home
                        authViewModel.signOut()
                    }
                )
            }

            AppDestination.Profile -> {
                ProfileScreen(
                    uiState = profileUiState,
                    onBack = { appDestination = AppDestination.Settings },
                    onRefresh = profileViewModel::loadProfile,
                    onSaveDisplayName = profileViewModel::saveDisplayName,
                    onMessagesShown = profileViewModel::clearMessages,
                    onProfileSaved = authViewModel::refreshCurrentUser
                )
            }
        }
    }
}

private enum class AppDestination {
    Home,
    Prediction,
    History,
    HistoryDetail,
    Settings,
    Profile
}
