package yusufs.turan.florai.ui.prediction

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yusufs.turan.florai.domain.flower.SupportedFlower
import yusufs.turan.florai.domain.flower.SupportedFlowers
import yusufs.turan.florai.domain.prediction.PredictionResult
import yusufs.turan.florai.domain.prediction.SelectedImage
import yusufs.turan.florai.ui.common.BackNavigationIcon
import java.io.File

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
    var showCamera by remember { mutableStateOf(false) }
    var showSupportedFlowersDialog by rememberSaveable { mutableStateOf(false) }
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
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCamera = true
        } else {
            onImageError("Kamera izni gerekli.")
        }
    }
    val openCamera = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            showCamera = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(predictionUiState.errorMessage) {
        val message = predictionUiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onPredictionErrorShown()
    }

    if (showCamera) {
        CameraCaptureScreen(
            onBack = { showCamera = false },
            onCaptured = { imageFile ->
                coroutineScope.launch {
                    val selectedImage = withContext(Dispatchers.IO) {
                        context.readCapturedImage(imageFile)
                    }
                    val bitmap = withContext(Dispatchers.IO) {
                        context.decodePreviewBitmap(imageFile)
                    }

                    if (selectedImage == null || bitmap == null) {
                        onImageError("Kamera gorseli okunamadi.")
                    } else {
                        previewBitmap = bitmap
                        onImageSelected(selectedImage)
                    }
                    showCamera = false
                }
            },
            onError = onImageError,
            modifier = modifier
        )
        return
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
                navigationIcon = { BackNavigationIcon(onBack = onBack) },
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
                hasResult = predictionUiState.result != null,
                onOpenCamera = openCamera,
                onPickImage = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onClearImage = {
                    previewBitmap = null
                    onClearImage()
                },
                onShowSupportedFlowers = { showSupportedFlowersDialog = true },
                onPredict = onPredict
            )

            predictionUiState.result?.let { result ->
                PredictionResultCard(result = result)
            }
        }
    }

    if (showSupportedFlowersDialog) {
        SupportedFlowersDialog(
            onDismiss = { showSupportedFlowersDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraCaptureScreen(
    onBack: () -> Unit,
    onCaptured: (File) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    DisposableEffect(cameraProviderFuture) {
        onDispose {
            if (cameraProviderFuture.isDone) runCatching {
                cameraProviderFuture.get().unbindAll()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Kamera") },
                navigationIcon = { BackNavigationIcon(onBack = onBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    PreviewView(viewContext).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        cameraProviderFuture.addListener(
                            {
                                runCatching {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also { preview ->
                                        preview.setSurfaceProvider(surfaceProvider)
                                    }
                                    val capture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                        .build()

                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        capture
                                    )
                                    imageCapture = capture
                                }.onFailure {
                                    onError("Kamera baslatilamadi.")
                                }
                            },
                            ContextCompat.getMainExecutor(viewContext)
                        )
                    }
                }
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
            ) {
                Button(
                    onClick = {
                        val capture = imageCapture
                        if (capture == null) {
                            onError("Kamera henuz hazir degil.")
                            return@Button
                        }

                        val imageFile = File(
                            context.cacheDir,
                            "florai_capture_${System.currentTimeMillis()}.jpg"
                        )
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile)
                            .build()

                        isCapturing = true
                        capture.takePicture(
                            outputOptions,
                            mainExecutor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(
                                    outputFileResults: ImageCapture.OutputFileResults
                                ) {
                                    isCapturing = false
                                    onCaptured(imageFile)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    isCapturing = false
                                    onError("Fotograf cekilemedi.")
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(52.dp),
                    enabled = imageCapture != null && !isCapturing
                ) {
                    if (isCapturing) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text("Kaydediliyor")
                        }
                    } else {
                        Text("Fotograf cek")
                    }
                }
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
                    onClick = onOpenCamera,
                    modifier = Modifier.weight(1f),
                    enabled = !isPredicting
                ) {
                    Text("Kamera")
                }
                OutlinedButton(
                    onClick = onPickImage,
                    modifier = Modifier.weight(1f),
                    enabled = !isPredicting
                ) {
                    Text("Galeri")
                }
            }

            OutlinedButton(
                onClick = onClearImage,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedImageName != null && !isPredicting
            ) {
                Text("Temizle")
            }

            OutlinedButton(
                onClick = onShowSupportedFlowers,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isPredicting
            ) {
                Text("Desteklenen cicekleri gor")
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
                    Text("Tahmin tamamlandi")
                } else {
                    Text("Tahmin et")
                }
            }
        }
    }
}

@Composable
private fun SupportedFlowersDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Desteklenen cicekler") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SupportedFlowers.items.forEach { flower ->
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
                            text = "Dusuk guvenli tahmin",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Model bu sonuc icin yeterince emin degil. Daha net, iyi aydinlatilmis ve cicegi merkeze alan bir fotografla tekrar deneyebilirsin.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
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

private fun Context.readCapturedImage(file: File): SelectedImage? {
    val bytes = file.takeIf { it.exists() }?.readBytes() ?: return null
    return SelectedImage(
        fileName = file.name,
        mimeType = "image/jpeg",
        bytes = bytes
    )
}

private fun Context.decodePreviewBitmap(uri: Uri): ImageBitmap? {
    return runCatching {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)).asImageBitmap()
    }.getOrNull()
}

private fun Context.decodePreviewBitmap(file: File): ImageBitmap? {
    return runCatching {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)).asImageBitmap()
    }.getOrNull()
}

private fun android.content.ContentResolver.getDisplayName(uri: Uri): String? {
    return query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
}
