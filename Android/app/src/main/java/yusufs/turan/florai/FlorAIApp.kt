package yusufs.turan.florai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import yusufs.turan.florai.ui.auth.AuthScreen
import yusufs.turan.florai.ui.auth.AuthViewModel
import yusufs.turan.florai.ui.history.PredictionHistoryDetailViewModel
import yusufs.turan.florai.ui.history.PredictionHistoryViewModel
import yusufs.turan.florai.ui.navigation.FlorAINavGraph
import yusufs.turan.florai.ui.prediction.PredictionViewModel
import yusufs.turan.florai.ui.profile.ProfileViewModel

@Composable
fun FlorAIApp(
    authViewModel: AuthViewModel = viewModel(),
    predictionViewModel: PredictionViewModel = viewModel(),
    predictionHistoryViewModel: PredictionHistoryViewModel = viewModel(),
    predictionHistoryDetailViewModel: PredictionHistoryDetailViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val predictionUiState by predictionViewModel.uiState.collectAsState()
    val predictionHistoryUiState by predictionHistoryViewModel.uiState.collectAsState()
    val predictionHistoryDetailUiState by predictionHistoryDetailViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val currentUser = authUiState.currentUser

    if (currentUser == null || !currentUser.emailVerified) {
        AuthScreen(
            uiState = authUiState,
            onSignIn = authViewModel::signIn,
            onRegister = authViewModel::register,
            onForgotPassword = authViewModel::sendPasswordResetEmail,
            onResendVerification = authViewModel::resendEmailVerification,
            onRefreshVerification = authViewModel::refreshEmailVerification,
            onSignOut = authViewModel::signOut,
            onMessagesShown = authViewModel::clearMessages
        )
        return
    }

    FlorAINavGraph(
        currentUser = currentUser,
        predictionUiState = predictionUiState,
        predictionHistoryUiState = predictionHistoryUiState,
        predictionHistoryDetailUiState = predictionHistoryDetailUiState,
        profileUiState = profileUiState,
        onImageSelected = predictionViewModel::setSelectedImage,
        onPredict = predictionViewModel::predict,
        onClearImage = predictionViewModel::clearSelectedImage,
        onPredictionErrorShown = predictionViewModel::clearError,
        onImageError = predictionViewModel::showError,
        onRefreshHistory = predictionHistoryViewModel::loadHistory,
        onDeleteHistoryItem = predictionHistoryViewModel::deleteItem,
        onDeleteHistory = predictionHistoryViewModel::deleteAll,
        onHistoryErrorShown = predictionHistoryViewModel::clearError,
        onLoadHistoryDetail = predictionHistoryDetailViewModel::loadDetail,
        onRefreshProfile = profileViewModel::loadProfile,
        onSaveDisplayName = profileViewModel::saveDisplayName,
        onProfileMessagesShown = profileViewModel::clearMessages,
        onProfileSaved = authViewModel::refreshCurrentUser,
        onSignOut = authViewModel::signOut
    )
}
