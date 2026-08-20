package yusufs.turan.florai.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import yusufs.turan.florai.domain.user.UserProfile
import yusufs.turan.florai.ui.common.BackNavigationIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSaveDisplayName: (String) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onMessagesShown: () -> Unit,
    onProfileSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editedDisplayName by rememberSaveable { mutableStateOf("") }
    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var deletePassword by rememberSaveable { mutableStateOf("") }
    val profile = uiState.profile

    LaunchedEffect(Unit) {
        onRefresh()
    }

    LaunchedEffect(profile?.displayName) {
        if (!isEditing) {
            editedDisplayName = profile?.displayName.orEmpty()
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        val message = uiState.errorMessage ?: uiState.successMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onMessagesShown()
    }

    LaunchedEffect(uiState.savedProfileVersion) {
        if (uiState.savedProfileVersion > 0) {
            isEditing = false
            showSaveDialog = false
            editedDisplayName = profile?.displayName.orEmpty()
            onProfileSaved()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
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
                .padding(20.dp)
        ) {
            when {
                uiState.isLoading && profile == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                profile == null -> {
                    EmptyProfile(
                        onRefresh = onRefresh,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    ProfileContent(
                        profile = profile,
                        isEditing = isEditing,
                        isSaving = uiState.isSaving,
                        isDeletingAccount = uiState.isDeletingAccount,
                        editedDisplayName = editedDisplayName,
                        onEdit = {
                            editedDisplayName = profile.displayName.orEmpty()
                            isEditing = true
                        },
                        onDisplayNameChange = { editedDisplayName = it },
                        onCancelEdit = {
                            editedDisplayName = profile.displayName.orEmpty()
                            isEditing = false
                        },
                        onRequestSave = { showSaveDialog = true },
                        onRequestDeleteAccount = {
                            deletePassword = ""
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Degisiklikleri kaydet") },
            text = { Text("Degisiklikleri kaydetmek istiyor musunuz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSaveDialog = false
                        onSaveDisplayName(editedDisplayName)
                    },
                    enabled = !uiState.isSaving
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveDialog = false },
                    enabled = !uiState.isSaving
                ) {
                    Text("Vazgec")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDeletingAccount) {
                    showDeleteDialog = false
                }
            },
            title = { Text("Hesabi sil") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Bu islem profil bilgilerini, tahmin gecmisini ve kaydedilen gorselleri siler. Devam etmek icin sifreni gir."
                    )
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isDeletingAccount,
                        singleLine = true,
                        label = { Text("Sifre") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onDeleteAccount(deletePassword) },
                    enabled = !uiState.isDeletingAccount && deletePassword.isNotBlank()
                ) {
                    if (uiState.isDeletingAccount) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Hesabi sil",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !uiState.isDeletingAccount
                ) {
                    Text("Vazgec")
                }
            }
        )
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    isEditing: Boolean,
    isSaving: Boolean,
    isDeletingAccount: Boolean,
    editedDisplayName: String,
    onEdit: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onRequestSave: () -> Unit,
    onRequestDeleteAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kullanici Bilgileri",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (!isEditing) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Duzenle"
                            )
                        }
                    }
                }

                if (isEditing) {
                    OutlinedTextField(
                        value = editedDisplayName,
                        onValueChange = onDisplayNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        singleLine = true,
                        label = { Text("Kullanici ismi") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancelEdit,
                            modifier = Modifier.weight(1f),
                            enabled = !isSaving
                        ) {
                            Text("Vazgec")
                        }
                        Button(
                            onClick = onRequestSave,
                            modifier = Modifier.weight(1f),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Kaydet")
                            }
                        }
                    }
                } else {
                    ProfileRow(
                        label = "Kullanici ismi",
                        value = profile.displayName ?: "Belirtilmedi"
                    )
                }

                ProfileRow(label = "E-posta", value = profile.email ?: "Belirtilmedi")
                ProfileRow(label = "Kullanici ID", value = profile.uid)
                ProfileRow(label = "Rol", value = profile.role)
                ProfileRow(
                    label = "Toplam tahmin",
                    value = profile.predictionCount.toString()
                )
                ProfileRow(label = "Olusturulma", value = profile.createdAt.toDisplayDate())
                ProfileRow(label = "Guncellenme", value = profile.updatedAt.toDisplayDate())
                ProfileRow(label = "Son etkinlik", value = profile.lastActiveAt.toDisplayDate())
            }
        }

        OutlinedButton(
            onClick = onRequestDeleteAccount,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && !isDeletingAccount,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            if (isDeletingAccount) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Hesabi sil")
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
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
private fun EmptyProfile(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Profil bilgileri alinamadi",
            style = MaterialTheme.typography.titleLarge
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
