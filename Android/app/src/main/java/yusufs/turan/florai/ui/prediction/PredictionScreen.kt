package yusufs.turan.florai.ui.prediction

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yusufs.turan.florai.domain.prediction.PredictionResult
import yusufs.turan.florai.domain.prediction.SelectedImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    predictionUiState: PredictionUiState,
    onImageSelected: (SelectedImage) -> Unit,
    onPredict: () -> Unit,
    onClearImage: () -> Unit,
    onPredictionErrorShown: () -> Unit,
    onImageError: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var previewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val selectedImage = withContext(Dispatchers.IO) {
                context.readSelectedImage(uri)
            }
            val bitmap = withContext(Dispatchers.IO) {
                context.decodePreviewBitmap(uri)
            }

            if (selectedImage == null || bitmap == null) {
                onImageError("Gorsel okunamadi.")
            } else {
                previewBitmap = bitmap
                onImageSelected(selectedImage)
            }
        }
    }

    LaunchedEffect(predictionUiState.errorMessage) {
        val message = predictionUiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onPredictionErrorShown()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tahmin")
                        Text(
                            text = "Gorsel sec ve cicegi tanimla",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text("Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ImagePickerCard(
                previewBitmap = previewBitmap,
                selectedImageName = predictionUiState.selectedImageName,
                selectedImageSizeBytes = predictionUiState.selectedImageSizeBytes,
                isPredicting = predictionUiState.isPredicting,
                onPickImage = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onClearImage = {
                    previewBitmap = null
                    onClearImage()
                },
                onPredict = onPredict
            )

            predictionUiState.result?.let { result ->
                PredictionResultCard(result = result)
            }
        }
    }
}

@Composable
private fun ImagePickerCard(
    previewBitmap: ImageBitmap?,
    selectedImageName: String?,
    selectedImageSizeBytes: Int?,
    isPredicting: Boolean,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
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
                        text = "Gorsel secilmedi",
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
                    onClick = onPickImage,
                    modifier = Modifier.weight(1f),
                    enabled = !isPredicting
                ) {
                    Text("Gorsel sec")
                }
                OutlinedButton(
                    onClick = onClearImage,
                    modifier = Modifier.weight(1f),
                    enabled = selectedImageName != null && !isPredicting
                ) {
                    Text("Temizle")
                }
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
                } else {
                    Text("Tahmin et")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PredictionResultCard(result: PredictionResult) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                ConfidenceBadge(
                    confidence = result.confidence,
                    lowConfidence = result.lowConfidence
                )
            }

            result.height?.let {
                InfoRow(label = "Boy", value = it)
            }

            if (result.bloomMonths.isNotEmpty()) {
                InfoChips(label = "Ciceklenme", values = result.bloomMonths)
            }

            if (result.habitats.isNotEmpty()) {
                InfoChips(label = "Yasam alanlari", values = result.habitats)
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
private fun ConfidenceBadge(
    confidence: Float,
    lowConfidence: Boolean
) {
    val containerColor = if (lowConfidence) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (lowConfidence) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = "${(confidence * 100).toInt()}%",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoChips(label: String, values: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

private fun Context.readSelectedImage(uri: Uri): SelectedImage? {
    val resolver = contentResolver
    val mimeType = resolver.getType(uri) ?: "image/jpeg"
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
    val fileName = resolver.getDisplayName(uri) ?: "flower-image.$extension"
    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null

    return SelectedImage(
        fileName = fileName,
        mimeType = mimeType,
        bytes = bytes
    )
}

private fun Context.decodePreviewBitmap(uri: Uri): ImageBitmap? {
    return runCatching {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)).asImageBitmap()
    }.getOrNull()
}

private fun android.content.ContentResolver.getDisplayName(uri: Uri): String? {
    return query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
}
