package yusufs.turan.florai.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

@Composable
fun PredictionHistoryImage(
    imagePath: String?,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    emptyText: String = "Görsel bekleniyor",
    contentDescription: String? = null
) {
    var resolvedImageUrl by remember(imagePath, imageUrl) { mutableStateOf<String?>(null) }
    var didFail by remember(imagePath, imageUrl) { mutableStateOf(false) }
    val containerModifier = modifier
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant)

    LaunchedEffect(imagePath, imageUrl) {
        resolvedImageUrl = null
        didFail = false

        val storagePath = imagePath?.takeIf { it.isNotBlank() }
        if (storagePath != null) {
            resolvedImageUrl = runCatching {
                FirebaseStorage.getInstance()
                    .reference
                    .child(storagePath)
                    .downloadUrl
                    .await()
                    .toString()
            }.getOrElse {
                didFail = true
                null
            }
            return@LaunchedEffect
        }

        resolvedImageUrl = imageUrl?.takeIf { it.isNotBlank() }
    }

    val url = resolvedImageUrl
    if (url == null) {
        ImageStateBox(
            text = if (didFail) "Görsel yüklenemedi" else emptyText,
            modifier = containerModifier
        )
        return
    }

    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = containerModifier,
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        },
        error = {
            ImageStateBox(
                text = "Görsel yüklenemedi",
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}

@Composable
private fun ImageStateBox(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
