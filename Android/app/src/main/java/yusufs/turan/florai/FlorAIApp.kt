package yusufs.turan.florai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import yusufs.turan.florai.ui.auth.AuthScreen
import yusufs.turan.florai.ui.auth.AuthViewModel
import yusufs.turan.florai.ui.home.HomeScreen

@Composable
fun FlorAIApp(
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser

    if (currentUser == null) {
        AuthScreen(
            uiState = uiState,
            onSignIn = authViewModel::signIn,
            onRegister = authViewModel::register,
            onErrorShown = authViewModel::clearError
        )
    } else {
        HomeScreen(
            user = currentUser,
            onSignOut = authViewModel::signOut
        )
    }
}
