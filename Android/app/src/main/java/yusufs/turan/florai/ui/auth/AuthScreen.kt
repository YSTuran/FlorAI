package yusufs.turan.florai.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import yusufs.turan.florai.ui.common.BackNavigationIcon

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
                        onForgotPasswordClick = { screenMode = AuthScreenMode.ForgotPassword },
                        onRegisterClick = { screenMode = AuthScreenMode.Register }
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

@Composable
private fun LoginContent(
    isSubmitting: Boolean,
    onSignIn: (String, String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        AuthHeader(
            title = "FlorAI",
            subtitle = "Hesabina giris yap"
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
            singleLine = true,
            label = { Text("E-posta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                singleLine = true,
                label = { Text("Sifre") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            TextButton(
                onClick = onForgotPasswordClick,
                enabled = !isSubmitting,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Sifremi unuttum")
            }
        }

        PrimaryAuthButton(
            text = "Giris yap",
            loadingText = "Giris yapiliyor",
            isSubmitting = isSubmitting,
            onClick = { onSignIn(email, password) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hesabin yok mu?",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onRegisterClick,
                enabled = !isSubmitting
            ) {
                Text("Kayit ol")
            }
        }
    }
}

@Composable
private fun RegisterContent(
    isSubmitting: Boolean,
    onRegister: (String, String, String, String) -> Unit,
    onBack: () -> Unit,
    onLoginClick: () -> Unit
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        BackNavigationIcon(onBack = onBack)
        AuthHeader(
            title = "Kayit ol",
            subtitle = "Yeni FlorAI hesabini olustur"
        )

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
            singleLine = true,
            label = { Text("Kullanici adi") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            )
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
            singleLine = true,
            label = { Text("E-posta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
            singleLine = true,
            label = { Text("Sifre") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
            singleLine = true,
            label = { Text("Sifre tekrar") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        PrimaryAuthButton(
            text = "Kayit ol",
            loadingText = "Kayit olusturuluyor",
            isSubmitting = isSubmitting,
            onClick = { onRegister(displayName, email, password, confirmPassword) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zaten hesabin var mi?",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onLoginClick,
                enabled = !isSubmitting
            ) {
                Text("Giris yap")
            }
        }
    }
}

@Composable
private fun ForgotPasswordContent(
    isSubmitting: Boolean,
    onForgotPassword: (String) -> Unit,
    onBack: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        BackNavigationIcon(onBack = onBack)
        AuthHeader(
            title = "Sifremi unuttum",
            subtitle = "Sifre sifirlama baglantisi e-posta adresine gonderilecek"
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
            singleLine = true,
            label = { Text("E-posta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        PrimaryAuthButton(
            text = "Sifirlama e-postasi gonder",
            loadingText = "Gonderiliyor",
            isSubmitting = isSubmitting,
            onClick = { onForgotPassword(email) }
        )
    }
}

@Composable
private fun EmailVerificationContent(
    email: String?,
    isSubmitting: Boolean,
    onRefreshVerification: () -> Unit,
    onResendVerification: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        AuthHeader(
            title = "E-postani dogrula",
            subtitle = "${email ?: "E-posta adresine"} gonderilen dogrulama baglantisini ac."
        )

        Text(
            text = "Dogrulama tamamlandiktan sonra Kontrol et butonuna basabilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PrimaryAuthButton(
            text = "Kontrol et",
            loadingText = "Kontrol ediliyor",
            isSubmitting = isSubmitting,
            onClick = onRefreshVerification
        )

        OutlinedButton(
            onClick = onResendVerification,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        ) {
            Text("Dogrulama e-postasini tekrar gonder")
        }

        TextButton(
            onClick = onSignOut,
            enabled = !isSubmitting,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Farkli hesapla giris yap")
        }
    }
}

@Composable
private fun AuthHeader(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PrimaryAuthButton(
    text: String,
    loadingText: String,
    isSubmitting: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = !isSubmitting
    ) {
        if (isSubmitting) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(loadingText)
            }
        } else {
            Text(text)
        }
    }
}

private enum class AuthScreenMode {
    Login,
    Register,
    ForgotPassword
}
