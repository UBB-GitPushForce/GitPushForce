package com.example.budgeting.android.ui.components.group

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.model.GroupStatistics

data class CategoryAmount(
    val categoryName: String,
    val amount: Double,
    val color: Color
)

@Composable
fun GroupPieChart(
    expenses: List<Expense>,
    categoryNameMap: Map<Int, String>,
    modifier: Modifier = Modifier
) {
    val categoryTotals = expenses
        .groupBy { expense ->
            categoryNameMap[expense.categoryId] ?: "Uncategorized"
        }
        .map { (category, expenseList) ->
            CategoryAmount(
                categoryName = category,
                amount = expenseList.sumOf { kotlin.math.abs(it.amount) },
                color = getColorForCategory(category)
            )
        }
        .sortedByDescending { it.amount }
    
    if (categoryTotals.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .then(Modifier.height(200.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No expenses to display",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    
    val totalAmount = categoryTotals.sumOf { it.amount }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pie Chart
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            PieChartCanvas(
                data = categoryTotals,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Legend
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryTotals.forEach { categoryAmount ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(12.dp)
                                .clip(CircleShape)
                                .background(categoryAmount.color)
                        )
                        Text(
                            text = categoryAmount.categoryName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = String.format("%.2f", categoryAmount.amount),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Total
        Text(
            text = "Total: ${String.format("%.2f", totalAmount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun GroupStatisticsPieChart(
    statistics: GroupStatistics,
    modifier: Modifier = Modifier
) {
    // Map statistics to pie slices
    val rawItems = listOf(
        CategoryAmount("Your share", statistics.myShareOfExpenses, getColorForCategory("Your share")),
        CategoryAmount("You paid", statistics.myTotalPaid, getColorForCategory("You paid")),
        CategoryAmount("Net balance for others", kotlin.math.abs(statistics.netBalancePaidForOthers), getColorForCategory("Net balance for others")),
        CategoryAmount("Rest of group", statistics.restOfGroupExpenses, getColorForCategory("Rest of group"))
    ).filter { it.amount > 0 }

    if (rawItems.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .then(Modifier.height(200.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No statistics to display",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val totalAmount = rawItems.sumOf { it.amount }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(220.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            PieChartCanvas(
                data = rawItems,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Legend
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rawItems.forEach { categoryAmount ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(12.dp)
                                .clip(CircleShape)
                                .background(categoryAmount.color)
                        )
                        Text(
                            text = categoryAmount.categoryName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = String.format("%.2f", categoryAmount.amount),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Text(
            text = "Total: ${String.format("%.2f", totalAmount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun PieChartCanvas(
    data: List<CategoryAmount>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.amount }
    if (total == 0.0) return
    
    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val innerRadius = radius * 0.5f // Donut chart style
        
        var startAngle = -90f // Start from top
        
        data.forEach { item ->
            val sweepAngle = (item.amount / total * 360f).toFloat()
            
            // Draw outer arc
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = radius - innerRadius)
            )
            
            startAngle += sweepAngle
        }
    }
}

private fun getColorForCategory(categoryName: String): Color {
    val colors = listOf(
        Color(0xFF7C3AED),
        Color(0xFF6B29D9),
        Color(0xFF059669),
        Color(0xFFDC2626),
        Color(0xFFF59E0B),
        Color(0xFF0EA5E9),
        Color(0xFFEF4444),
        Color(0xFF8B5CF6),
        Color(0xFF10B981),
        Color(0xFFF97316)
    )
    
    val index = categoryName.hashCode().absoluteValue % colors.size
    return colors[index]
}

private val Int.absoluteValue: Int
    get() = if (this < 0) -this else this

