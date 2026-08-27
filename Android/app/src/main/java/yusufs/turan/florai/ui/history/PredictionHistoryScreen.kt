package yusufs.turan.florai.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import yusufs.turan.florai.ui.common.BackNavigationIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionHistoryScreen(
    uiState: PredictionHistoryUiState,
    onBack: (() -> Unit)? = null,
    onOpenDetails: (PredictionHistoryItem) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
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
                        Text("Tahmin Geçmişi")
                        Text(
                            text = "${uiState.items.size} kayıt",
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
                            Text("Tümünü sil")
                        }
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
                                onOpenDetails = { onOpenDetails(item) },
                                onDelete = { itemPendingDelete = item }
                            )
                        }

                        if (uiState.nextCursor != null) {
                            item {
                                LoadMoreHistoryButton(
                                    isLoadingMore = uiState.isLoadingMore,
                                    enabled = uiState.canLoadMore,
                                    onLoadMore = onLoadMore
                                )
                            }
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
            text = { Text("${item.displayName} tahmin kaydı silinsin mi?") },
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
                    Text("Vazgeç")
                }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Tüm geçmişi sil") },
            text = { Text("Tüm tahmin geçmişin kalıcı olarak silinsin mi?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllDialog = false
                        onDeleteAll()
                    }
                ) {
                    Text("Tümünü sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}
