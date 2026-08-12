package yusufs.turan.florai.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import yusufs.turan.florai.domain.prediction.PredictionHistoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionHistoryScreen(
    uiState: PredictionHistoryUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onDeleteItem: (String) -> Unit,
    onDeleteAll: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var itemPendingDelete by remember { mutableStateOf<PredictionHistoryItem?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onRefresh()
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onErrorShown()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tahmin Gecmisi")
                        Text(
                            text = "${uiState.items.size} kayit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (uiState.hasItems) {
                        TextButton(
                            onClick = { showDeleteAllDialog = true },
                            enabled = !uiState.isDeleting
                        ) {
                            Text("Tumunu sil")
                        }
                    }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            when {
                uiState.isLoading && uiState.items.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.items.isEmpty() -> {
                    EmptyHistory(
                        onRefresh = onRefresh,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.items, key = { it.id }) { item ->
                            HistoryItemCard(
                                item = item,
                                isDeleting = uiState.isDeleting,
                                onDelete = { itemPendingDelete = item }
                            )
                        }
                    }
                }
            }
        }
    }

    itemPendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemPendingDelete = null },
            title = { Text("Tahmini sil") },
            text = { Text("${item.displayName} tahmin kaydi silinsin mi?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemPendingDelete = null
                        onDeleteItem(item.id)
                    }
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemPendingDelete = null }) {
                    Text("Vazgec")
                }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Tum gecmisi sil") },
            text = { Text("Tum tahmin gecmisin kalici olarak silinsin mi?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllDialog = false
                        onDeleteAll()
                    }
                ) {
                    Text("Tumunu sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Vazgec")
                }
            }
        )
    }
}

@Composable
private fun HistoryItemCard(
    item: PredictionHistoryItem,
    isDeleting: Boolean,
    onDelete: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.displayName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = item.createdAt.toDisplayDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ConfidenceBadge(
                    confidence = item.confidence,
                    lowConfidence = item.lowConfidence
                )
            }

            if (item.lowConfidence) {
                Text(
                    text = "Dusuk guvenli tahmin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedButton(
                onClick = onDelete,
                enabled = !isDeleting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tahmini sil")
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
private fun EmptyHistory(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Gecmis kaydi yok",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Tahmin yaptikca kayitlar burada gorunecek.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onRefresh) {
            Text("Yenile")
        }
    }
}

private fun String?.toDisplayDate(): String {
    if (isNullOrBlank()) return "Tarih bilgisi yok"
    return replace("T", " ")
        .substringBefore(".")
        .substringBefore("+")
        .take(16)
}
