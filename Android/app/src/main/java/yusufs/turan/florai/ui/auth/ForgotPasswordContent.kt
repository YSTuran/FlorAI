package yusufs.turan.florai.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import yusufs.turan.florai.ui.common.BackNavigationIcon

@Composable
fun ForgotPasswordContent(
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
