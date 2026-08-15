package yusufs.turan.florai.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import yusufs.turan.florai.data.auth.AuthUser

@Composable
fun HomeScreen(
    user: AuthUser,
    onOpenPrediction: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "FlorAI",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = user.displayName?.takeIf { it.isNotBlank() }
                        ?: user.email
                        ?: "Oturum acik",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            NavigationCard(
                title = "Tahmin",
                description = "Galeriden bir cicek gorseli secip model tahmini al.",
                actionText = "Tahmin ekranina git",
                onClick = onOpenPrediction
            )

            NavigationCard(
                title = "Tahmin Gecmisi",
                description = "Daha once yaptigin tahminleri gor ve kayitlarini yonet.",
                actionText = "Gecmise git",
                onClick = onOpenHistory,
                outlined = true
            )

            NavigationCard(
                title = "Ayarlar",
                description = "Hesap ve uygulama ayarlarini yonet.",
                actionText = "Ayarlara git",
                onClick = onOpenSettings,
                outlined = true
            )
        }
    }
}

@Composable
private fun NavigationCard(
    title: String,
    description: String,
    actionText: String,
    onClick: () -> Unit,
    outlined: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (outlined) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(actionText)
                }
            } else {
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(actionText)
                }
            }
        }
    }
}
