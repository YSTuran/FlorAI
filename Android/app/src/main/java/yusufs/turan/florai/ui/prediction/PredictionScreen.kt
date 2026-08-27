package yusufs.turan.florai.ui.prediction

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yusufs.turan.florai.domain.flower.SupportedFlower
import yusufs.turan.florai.domain.prediction.SelectedImage
import yusufs.turan.florai.ui.common.BackNavigationIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    predictionUiState: PredictionUiState,
    supportedFlowers: List<SupportedFlower>,
    onImageSelected: (SelectedImage) -> Unit,
    onPredict: () -> Unit,
    onClearImage: () -> Unit,
    onPredictionErrorShown: () -> Unit,
    onImageError: (String) -> Unit,
    onBack: (() -> Unit)? = null,
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
            val preparedImage = withContext(Dispatchers.IO) {
                context.prepareSelectedImage(uri)
            }

            if (preparedImage == null) {
                onImageError("Görsel okunamadı.")
            } else {
                previewBitmap = preparedImage.previewBitmap
                onImageSelected(preparedImage.selectedImage)
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
                    val preparedImage = withContext(Dispatchers.IO) {
                        context.prepareCapturedImage(imageFile)
                    }

                    if (preparedImage == null) {
                        onImageError("Kamera görseli okunamadı.")
                    } else {
                        previewBitmap = preparedImage.previewBitmap
                        onImageSelected(preparedImage.selectedImage)
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
                            text = "Görsel seç ve çiçeği tanımla",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        BackNavigationIcon(onBack = onBack)
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
            flowers = supportedFlowers,
            onDismiss = { showSupportedFlowersDialog = false }
        )
    }
}
