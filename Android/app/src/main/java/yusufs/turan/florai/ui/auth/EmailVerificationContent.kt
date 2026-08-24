package yusufs.turan.florai.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EmailVerificationContent(
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
            title = "E-postanı doğrula",
            subtitle = "${email ?: "E-posta adresine"} gönderilen doğrulama bağlantısını aç."
        )

        Text(
            text = "Doğrulama tamamlandıktan sonra Kontrol et butonuna basabilirsin.",
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
            Text("Doğrulama e-postasını tekrar gönder")
        }

        TextButton(
            onClick = onSignOut,
            enabled = !isSubmitting,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Farklı hesapla giriş yap")
        }
    }
}
