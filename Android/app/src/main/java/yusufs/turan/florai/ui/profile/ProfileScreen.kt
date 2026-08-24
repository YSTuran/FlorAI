package yusufs.turan.florai.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        SaveProfileChangesDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showSaveDialog = false },
            onConfirm = {
                showSaveDialog = false
                onSaveDisplayName(editedDisplayName)
            }
        )
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            password = deletePassword,
            isDeletingAccount = uiState.isDeletingAccount,
            onPasswordChange = { deletePassword = it },
            onDismiss = { showDeleteDialog = false },
            onConfirm = { onDeleteAccount(deletePassword) }
        )
    }
}
