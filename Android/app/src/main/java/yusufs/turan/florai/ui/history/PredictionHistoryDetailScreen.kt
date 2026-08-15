package yusufs.turan.florai.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import yusufs.turan.florai.domain.prediction.PredictionHistoryItem
import yusufs.turan.florai.domain.prediction.PredictionScore
import yusufs.turan.florai.ui.common.BackNavigationIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionHistoryDetailScreen(
    item: PredictionHistoryItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tahmin Detayi")
                        Text(
                            text = item.createdAt.toDisplayDate(),
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PredictionHistoryImage(
                imageUrl = item.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
                emptyText = "Gorsel henuz Storage'a yuklenmedi"
            )

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
                    Text(
                        text = item.displayName,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    DetailRow(label = "Kesinlik", value = item.confidence.toPercentText())
                    DetailRow(label = "Model etiketi", value = item.modelLabel)
                    if (item.lowConfidence) {
                        Text(
                            text = "Bu kayit dusuk guvenli tahmin olarak isaretlenmis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

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
                    Text(
                        text = "Tum sinif skorlari",
                        style = MaterialTheme.typography.titleLarge
                    )
                    item.detailScores().forEach { score ->
                        ScoreRow(score = score)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
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
private fun ScoreRow(score: PredictionScore) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = score.displayName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = score.modelLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Text(
                text = score.confidence.toPercentText(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun PredictionHistoryItem.detailScores(): List<PredictionScore> {
    if (topPredictions.isNotEmpty()) return topPredictions.sortedByDescending { it.confidence }
    return listOf(
        PredictionScore(
            classId = classId,
            modelLabel = modelLabel,
            flowerId = predictedFlowerId,
            displayName = displayName,
            confidence = confidence
        )
    )
}

private fun Float.toPercentText(): String {
    return "${(coerceIn(0f, 1f) * 100).toInt()}%"
}
