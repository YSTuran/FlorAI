package yusufs.turan.florai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import yusufs.turan.florai.ui.auth.AuthScreen
import yusufs.turan.florai.ui.auth.AuthViewModel
import yusufs.turan.florai.ui.flower.FlowerCatalogViewModel
import yusufs.turan.florai.ui.history.PredictionHistoryDetailViewModel
import yusufs.turan.florai.ui.history.PredictionHistoryViewModel
import yusufs.turan.florai.ui.navigation.FlorAINavGraph
import yusufs.turan.florai.ui.prediction.PredictionViewModel
import yusufs.turan.florai.ui.profile.ProfileViewModel

@Composable
fun FlorAIApp(
    authViewModel: AuthViewModel = hiltViewModel(),
    predictionViewModel: PredictionViewModel = hiltViewModel(),
    predictionHistoryViewModel: PredictionHistoryViewModel = hiltViewModel(),
    predictionHistoryDetailViewModel: PredictionHistoryDetailViewModel = hiltViewModel(),
    flowerCatalogViewModel: FlowerCatalogViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val predictionUiState by predictionViewModel.uiState.collectAsState()
    val predictionHistoryUiState by predictionHistoryViewModel.uiState.collectAsState()
    val predictionHistoryDetailUiState by predictionHistoryDetailViewModel.uiState.collectAsState()
    val flowerCatalogUiState by flowerCatalogViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val currentUser = authUiState.currentUser
    val verifiedUserUid = currentUser?.takeIf { it.emailVerified }?.uid
    var lastVerifiedUserUid by remember { mutableStateOf<String?>(null) }

    fun resetSessionState() {
        predictionViewModel.resetSessionState()
        predictionHistoryViewModel.resetSessionState()
        predictionHistoryDetailViewModel.resetSessionState()
        flowerCatalogViewModel.resetSessionState()
        profileViewModel.resetSessionState()
    }

    fun signOutAndResetSession() {
        resetSessionState()
        lastVerifiedUserUid = null
        authViewModel.signOut()
    }

    LaunchedEffect(verifiedUserUid) {
        if (lastVerifiedUserUid != verifiedUserUid) {
            resetSessionState()
            lastVerifiedUserUid = verifiedUserUid
            if (verifiedUserUid != null) {
                flowerCatalogViewModel.loadFlowers(forceRefresh = true)
            }
        }
    }

    if (currentUser == null || !currentUser.emailVerified) {
        AuthScreen(
            uiState = authUiState,
            onSignIn = authViewModel::signIn,
            onRegister = authViewModel::register,
            onForgotPassword = authViewModel::sendPasswordResetEmail,
            onResendVerification = authViewModel::resendEmailVerification,
            onRefreshVerification = authViewModel::refreshEmailVerification,
            onSignOut = { signOutAndResetSession() },
            onMessagesShown = authViewModel::clearMessages
        )
        return
    }

    FlorAINavGraph(
        currentUser = currentUser,
        predictionUiState = predictionUiState,
        predictionHistoryUiState = predictionHistoryUiState,
        predictionHistoryDetailUiState = predictionHistoryDetailUiState,
        flowerCatalogUiState = flowerCatalogUiState,
        profileUiState = profileUiState,
        onImageSelected = predictionViewModel::setSelectedImage,
        onPredict = predictionViewModel::predict,
        onClearImage = predictionViewModel::clearSelectedImage,
        onPredictionErrorShown = predictionViewModel::clearError,
        onImageError = predictionViewModel::showError,
        onRefreshHistory = predictionHistoryViewModel::loadHistory,
        onLoadMoreHistory = predictionHistoryViewModel::loadMore,
        onDeleteHistoryItem = predictionHistoryViewModel::deleteItem,
        onDeleteHistory = predictionHistoryViewModel::deleteAll,
        onHistoryErrorShown = predictionHistoryViewModel::clearError,
        onLoadHistoryDetail = predictionHistoryDetailViewModel::loadDetail,
        onRefreshProfile = profileViewModel::loadProfile,
        onSaveDisplayName = profileViewModel::saveDisplayName,
        onDeleteAccount = profileViewModel::deleteAccount,
        onProfileMessagesShown = profileViewModel::clearMessages,
        onProfileSaved = authViewModel::refreshCurrentUser,
        onSignOut = { signOutAndResetSession() }
    )
}
