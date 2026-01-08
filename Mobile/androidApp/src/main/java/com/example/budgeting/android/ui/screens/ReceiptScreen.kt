package com.example.budgeting.android.ui.screens

import ReceiptViewModelFactory
import android.Manifest
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.data.model.ProcessedItem
import com.example.budgeting.android.ui.viewmodels.ReceiptUiState
import com.example.budgeting.android.ui.viewmodels.ReceiptViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val vm: ReceiptViewModel = viewModel(
        factory = ReceiptViewModelFactory(context.applicationContext as Application)
    )

    val uiState by vm.uiState.collectAsState()
    val scannedItems by vm.scannedItems.collectAsState()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap: Bitmap? ->
            bitmap?.let {
                val uri = saveBitmapToCacheUri(context, it)
                vm.processReceiptImage(uri)
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) cameraLauncher.launch(null)
        }
    )

    fun launchCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED) {
            cameraLauncher.launch(null)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipts", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (uiState) {
                        is ReceiptUiState.Idle -> EmptyStateContent()
                        is ReceiptUiState.Loading -> {  }

                        is ReceiptUiState.Reviewing -> {
                            if (scannedItems.isNotEmpty()) {
                                ReceiptReviewDialog(
                                    items = scannedItems,
                                    onConfirm = { selectedItems ->
                                        scope.launch { vm.saveExpenses(selectedItems) }
                                    },
                                    onCancel = { vm.cancelReview() }
                                )
                            }
                        }
                        is ReceiptUiState.Success -> {
                            ResultAlertDialog(
                                title = "Done",
                                message = (uiState as ReceiptUiState.Success).message,
                                onDismiss = { vm.dismissError() }
                            )
                        }
                        is ReceiptUiState.Error -> {
                            ResultAlertDialog(
                                title = "Error",
                                message = (uiState as ReceiptUiState.Error).message,
                                isError = true,
                                onDismiss = { vm.dismissError() }
                            )
                        }
                    }
                }

                PaddingValues(16.dp).let {
                    Button(
                        onClick = { launchCamera() },
                        enabled = uiState is ReceiptUiState.Idle || uiState is ReceiptUiState.Error || uiState is ReceiptUiState.Success,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Scan Receipt", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            if (uiState is ReceiptUiState.Loading) {
                val message = (uiState as ReceiptUiState.Loading).message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                        .zIndex(2f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 6.dp
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ReceiptReviewDialog(
    items: List<ProcessedItem>,
    onConfirm: (List<ProcessedItem>) -> Unit,
    onCancel: () -> Unit
) {
    val selectedItems = remember { mutableStateListOf<ProcessedItem>().apply { addAll(items) } }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Review Items",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${selectedItems.size} of ${items.size} items selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { item ->
                        val isSelected = selectedItems.contains(item)
                        SelectableItemRow(
                            item = item,
                            isSelected = isSelected,
                            onToggle = {
                                if (isSelected) selectedItems.remove(item)
                                else selectedItems.add(item)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                val total = selectedItems.sumOf { it.price }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Selected", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedItems.toList()) },
                        enabled = selectedItems.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save (${selectedItems.size})")
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableItemRow(
    item: ProcessedItem,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox for selection
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if(isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = if(isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if(isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f)
            )
            Text(
                text = item.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "$${String.format("%.2f", item.price)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f)
        )
    }
}

@Composable
fun EmptyStateContent() {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "No receipt scanned",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Snap a photo of your receipt to automatically extract and save expenses.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ResultAlertDialog(
    title: String,
    message: String,
    isError: Boolean = false,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
        title = {
            Text(
                text = title,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        },
        text = { Text(message) }
    )
}

@Composable
fun ScannedItemRow(item: ProcessedItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "$${String.format("%.2f", item.price)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun saveBitmapToCacheUri(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "scan_${UUID.randomUUID()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
    }
    return Uri.fromFile(file)
}
