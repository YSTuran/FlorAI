package yusufs.turan.florai.ui.prediction

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import yusufs.turan.florai.domain.flower.SupportedFlower
import yusufs.turan.florai.domain.prediction.PredictionResult

@Composable
internal fun ImagePickerCard(
    previewBitmap: ImageBitmap?,
    selectedImageName: String?,
    selectedImageSizeBytes: Int?,
    isPredicting: Boolean,
    hasResult: Boolean,
    onOpenCamera: () -> Unit,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
    onShowSupportedFlowers: () -> Unit,
    onPredict: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Tahmin",
                style = MaterialTheme.typography.titleLarge
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap == null) {
                    Text(
                        text = "Görsel seçilmedi",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Image(
                        bitmap = previewBitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            selectedImageName?.let { name ->
                Text(
                    text = "$name${selectedImageSizeBytes?.let { " - ${it / 1024} KB" }.orEmpty()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenCamera,
                    modifier = Modifier.weight(1f),
                    enabled = !isPredicting
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                    Text("Kamera")
                }
                OutlinedButton(
                    onClick = onPickImage,
                    modifier = Modifier.weight(1f),
                    enabled = !isPredicting
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                    Text("Galeri")
                }
            }

            OutlinedButton(
                onClick = onClearImage,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedImageName != null && !isPredicting
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                Text("Temizle")
            }

            OutlinedButton(
                onClick = onShowSupportedFlowers,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isPredicting
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterVintage,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                Text("Desteklenen çiçekleri gör")
            }

            Button(
                onClick = onPredict,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = selectedImageName != null && !isPredicting
            ) {
                if (isPredicting) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text("Tahmin ediliyor")
                    }
                } else if (hasResult) {
                    Text("Tahmin tamamlandı")
                } else {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                    Text("Tahmin et")
                }
            }
        }
    }
}

@Composable
internal fun SupportedFlowersDialog(
    flowers: List<SupportedFlower>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Desteklenen çiçekler") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                flowers.forEach { flower ->
                    SupportedFlowerRow(flower = flower)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tamam")
            }
        }
    )
}

@Composable
private fun SupportedFlowerRow(flower: SupportedFlower) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = flower.displayName,
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = flower.scientificName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun PredictionResultCard(result: PredictionResult) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = result.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                result.scientificName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (result.lowConfidence) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Düşük güvenli tahmin",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Model bu sonuç için yeterince emin değil. Daha net, iyi aydınlatılmış ve çiçeği merkeze alan bir fotoğrafla tekrar deneyebilirsin.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            result.height?.let {
                InfoRow(label = "Boy", value = it)
            }

            if (result.bloomMonths.isNotEmpty()) {
                InfoChips(label = "Çiçeklenme", values = result.bloomMonths)
            }

            if (result.habitats.isNotEmpty()) {
                InfoChips(label = "Yaşam alanları", values = result.habitats)
            }

            result.details?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (result.extraFacts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Ek bilgiler",
                        style = MaterialTheme.typography.titleSmall
                    )
                    result.extraFacts.forEach { fact ->
                        Text(
                            text = "- $fact",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoChips(label: String, values: List<String>) {
    val chipScrollState = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall
        )
        Row(
            modifier = Modifier.horizontalScroll(chipScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            values.forEach { value ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        text = value,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
