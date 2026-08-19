package yusufs.turan.florai.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onResendVerification: () -> Unit,
    onRefreshVerification: () -> Unit,
    onSignOut: () -> Unit,
    onMessagesShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var screenMode by rememberSaveable { mutableStateOf(AuthScreenMode.Login) }
    val currentUser = uiState.currentUser

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        val message = uiState.errorMessage ?: uiState.successMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onMessagesShown()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                !uiState.isAuthReady -> {
                    CircularProgressIndicator()
                }

                currentUser?.emailVerified == false -> {
                    EmailVerificationContent(
                        email = currentUser.email,
                        isSubmitting = uiState.isSubmitting,
                        onRefreshVerification = onRefreshVerification,
                        onResendVerification = onResendVerification,
                        onSignOut = onSignOut
                    )
                }

                screenMode == AuthScreenMode.Login -> {
                    LoginContent(
                        isSubmitting = uiState.isSubmitting,
                        onSignIn = onSignIn,
                        onForgotPasswordClick = {
                            screenMode = AuthScreenMode.ForgotPassword
                        },
                        onRegisterClick = {
                            screenMode = AuthScreenMode.Register
                        }
                    )
                }

                screenMode == AuthScreenMode.Register -> {
                    RegisterContent(
                        isSubmitting = uiState.isSubmitting,
                        onRegister = onRegister,
                        onBack = { screenMode = AuthScreenMode.Login },
                        onLoginClick = { screenMode = AuthScreenMode.Login }
                    )
                }

                screenMode == AuthScreenMode.ForgotPassword -> {
                    ForgotPasswordContent(
                        isSubmitting = uiState.isSubmitting,
                        onForgotPassword = onForgotPassword,
                        onBack = { screenMode = AuthScreenMode.Login }
                    )
                }
            }
        }
    }
}
