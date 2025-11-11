package com.example.budgeting.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.budgeting.android.data.model.Receipt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsScreen() {
    // Mock data
    val mockReceipts = listOf(
        Receipt(id = "mock-1", merchant = "Whole Foods Market", category = "Groceries", amount = 120.0),
        Receipt(id = "mock-2", merchant = "The Italian Place", category = "Dining", amount = 75.5),
        Receipt(id = "mock-3", merchant = "Fashion Boutique", category = "Shopping", amount = 250.0),
        Receipt(id = "mock-4", merchant = "Electric Company", category = "Utilities", amount = 150.0),
        Receipt(id = "mock-5", merchant = "Movie Theater", category = "Entertainment", amount = 30.0),
        Receipt(id = "mock-6", merchant = "Gas Station", category = "Transportation", amount = 45.0)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Receipts", color = MaterialTheme.colorScheme.onBackground) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                items(mockReceipts) { receipt ->
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
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }

        Text(
            text = "$${"%.2f".format(amount)}",
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
