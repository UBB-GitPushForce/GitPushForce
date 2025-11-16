package com.example.budgeting.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import coil.compose.AsyncImage
import com.example.budgeting.android.data.model.Receipt
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen() {
    val context = LocalContext.current
    val receipts = remember {
        mutableStateListOf<Receipt>()
    }

    // Camera result forwarder that the dialog can set
    val cameraResultHandler = remember { mutableStateOf<(String?) -> Unit>({}) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* permission flow handled by launchCameraWith */ }
    )

    // Camera launcher (returns a Bitmap preview) and forwards to current handler
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap: Bitmap? ->
            val uriString = bitmap?.let { saveBitmapToCache(context.cacheDir, it) }
            cameraResultHandler.value.invoke(uriString)
        }
    )

    fun launchCameraWith(onPicture: (String?) -> Unit) {
        cameraResultHandler.value = onPicture
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PermissionChecker.PERMISSION_GRANTED

        if (hasPermission) {
            cameraLauncher.launch(null)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Receipts", color = MaterialTheme.colorScheme.onBackground) }
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Add receipt")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (receipts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("🧾", style = MaterialTheme.typography.displayLarge)
                        Text(
                            text = "No receipts yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap 'Add receipt' to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    items(receipts) { receipt ->
                        ReceiptRow(
                            title = receipt.merchant,
                            subtitle = receipt.category,
                            amount = receipt.amount,
                            thumbnailUrl = receipt.thumbnailUrl
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddReceiptDialog(
                onDismiss = { showAddDialog = false },
                onScan = { onPicture -> launchCameraWith(onPicture) },
                onAdd = { merchant, category, amount, thumbnailUrl ->
                    receipts.add(
                        0,
                        Receipt(
                            id = UUID.randomUUID().toString(),
                            merchant = merchant.ifBlank { "Untitled" },
                            category = category.ifBlank { "Uncategorized" },
                            amount = amount,
                            thumbnailUrl = thumbnailUrl
                        )
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

private fun saveBitmapToCache(cacheDir: File, bitmap: Bitmap): String {
    val file = File(cacheDir, "receipt-${UUID.randomUUID()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
    }
    return file.toURI().toString()
}

@Composable
private fun AddReceiptDialog(
    onDismiss: () -> Unit,
    onScan: (onPicture: (String?) -> Unit) -> Unit,
    onAdd: (merchant: String, category: String, amount: Double, thumbnailUrl: String?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("✕", color = MaterialTheme.colorScheme.onBackground)
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "New Receipt",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                }

                // Inputs
                val merchant = remember { mutableStateOf("") }
                val category = remember { mutableStateOf("") }
                val amountText = remember { mutableStateOf("") }
                val thumbnail = remember { mutableStateOf<String?>(null) }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextField(
                        value = merchant.value,
                        onValueChange = { merchant.value = it },
                        placeholder = { Text("Merchant") },
                        label = { Text("Merchant") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = category.value,
                        onValueChange = { category.value = it },
                        placeholder = { Text("Category") },
                        label = { Text("Category") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = amountText.value,
                        onValueChange = { input ->
                            // Allow only digits and at most one dot
                            val filtered = buildString {
                                var dotSeen = false
                                input.forEach { ch ->
                                    if (ch.isDigit()) append(ch)
                                    else if (ch == '.' && !dotSeen) {
                                        append(ch)
                                        dotSeen = true
                                    }
                                }
                            }
                            amountText.value = filtered
                        },
                        placeholder = { Text("Amount") },
                        label = { Text("Amount") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Thumbnail preview + Scan button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(
                                modifier = Modifier.size(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (thumbnail.value != null) {
                                    AsyncImage(
                                        model = thumbnail.value,
                                        contentDescription = null,
                                        modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("🧾")
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onScan { uri -> thumbnail.value = uri }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Camera,
                                contentDescription = "Scan"
                            )
                            Spacer(Modifier.size(8.dp))
                            Text("Scan")
                        }
                    }
                }

                Button(
                    onClick = {
                        val amount = amountText.value.toDoubleOrNull() ?: 0.0
                        onAdd(
                            merchant.value.trim(),
                            category.value.trim(),
                            amount,
                            thumbnail.value
                        )
                    },
                    enabled = merchant.value.isNotBlank(),
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Add receipt")
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    title: String,
    subtitle: String,
    amount: Double,
    thumbnailUrl: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailUrl != null) {
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("🧾")
                }
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface)
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Text(
            text = "$${"%.2f".format(amount)}",
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
