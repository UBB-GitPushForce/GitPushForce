package com.example.budgeting.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.data.model.CategoryCount
import com.example.budgeting.android.data.model.CategoryTotal
import com.example.budgeting.android.data.model.MonthlyTotal
import com.example.budgeting.android.ui.viewmodels.AnalyticsViewModel
import com.example.budgeting.android.ui.viewmodels.AnalyticsViewModelFactory
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.time.LocalDate
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import com.example.budgeting.android.data.model.*
import com.example.budgeting.android.ui.viewmodels.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {
    val context = LocalContext.current
    val viewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(context)
    )

    val mode by viewModel.mode.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val from by viewModel.dateFrom.collectAsState()
    val to by viewModel.dateTo.collectAsState()
    val categoryCounts by viewModel.categoryCounts.collectAsState()
    val categoryAmounts by viewModel.categoryAmounts.collectAsState()
    val monthlyTotals by viewModel.monthlyTotals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAnalytics() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analytics",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(8.dp))

            ModeSelector(selected = mode, onSelected = { viewModel.setMode(it) })

            Spacer(Modifier.height(12.dp))

            // ---------------- FILTER CARD ----------------
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnalyticsFilterRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.setCategory(it) },
                    from = from,
                    to = to,
                    onFromSelected = { viewModel.setDateFrom(it) },
                    onToSelected = { viewModel.setDateTo(it) }
                )
            }

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> CenterLoading()
                error != null -> CenterError(error!!)
                else -> AnalyticsContent(
                    categoryCounts,
                    categoryAmounts,
                    monthlyTotals
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    from: LocalDate?,
    to: LocalDate?,
    onFromSelected: (LocalDate) -> Unit,
    onToSelected: (LocalDate) -> Unit
) {
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // DATE RANGE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { showFromDatePicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(from?.toString() ?: "From")
            }

            TextButton(onClick = { showToDatePicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(to?.toString() ?: "To")
            }
        }
    }

    if (showFromDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = from?.toEpochDay()?.times(86400000L)
        )
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onFromSelected(LocalDate.ofEpochDay(it / 86400000L))
                    }
                    showFromDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) { DatePicker(state = state) }
    }

    if (showToDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = to?.toEpochDay()?.times(86400000L)
        )
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onToSelected(LocalDate.ofEpochDay(it / 86400000L))
                    }
                    showToDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) { DatePicker(state = state) }
    }
}

@Composable
fun AnalyticsContent(
    categoryCounts: List<CategoryCount>,
    categoryAmounts: List<CategoryTotal>,
    monthlyTotals: List<MonthlyTotal>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        AnalyticsCard("Expenses per category") {
            CategoryCountBarChart(categoryCounts)
        }

        AnalyticsCard("Total amount per category") {
            CategoryAmountBarChart(categoryAmounts)
        }

        AnalyticsCard("Monthly trend") {
            MonthlyLineChart(monthlyTotals)
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}


// ----------------- REMAINING ANALYTICS CHARTS -----------------
@Composable
fun AnalyticsContentOLD(
    categoryCounts: List<CategoryCount>,
    categoryAmounts: List<CategoryTotal>,
    monthlyTotals: List<MonthlyTotal>
) {
    Column {
        Text("Number of Expenses per Category", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        CategoryCountBarChart(categoryCounts)

        Spacer(Modifier.height(24.dp))

        Text("Cumulative Amount per Category", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        CategoryAmountBarChart(categoryAmounts)

        Spacer(Modifier.height(24.dp))

        Text("Monthly Trend", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        MonthlyLineChart(monthlyTotals)
    }
}

@Composable
fun CategoryCountBarChart(data: List<CategoryCount>) {
    if (data.isEmpty()) {
        Text("No data available")
        return
    }

    val entries = data.mapIndexed { index, item ->
        FloatEntry(index.toFloat(), item.count.toFloat())
    }

    val model = entryModelOf(entries)

    val bottomAxis = rememberBottomAxis(
        valueFormatter = { value: Float, _: ChartValues ->
            val idx = value.toInt()
            data.getOrNull(idx)?.category ?: ""
        }
    )


    Chart(
        chart = columnChart(),
        model = model,
        startAxis = rememberStartAxis(),
        bottomAxis = bottomAxis
    )
}


@Composable
fun CategoryAmountBarChart(data: List<CategoryTotal>) {
    if (data.isEmpty()) {
        Text("No data available")
        return
    }

    val entries = data.mapIndexed { index, item ->
        FloatEntry(index.toFloat(), item.total)
    }

    val model = entryModelOf(entries)

    val bottomAxis = rememberBottomAxis(
        valueFormatter = { value: Float, _: ChartValues ->
            val idx = value.toInt()
            data.getOrNull(idx)?.category ?: ""
        }
    )


    Chart(
        chart = columnChart(),
        model = model,
        startAxis = rememberStartAxis(),
        bottomAxis = bottomAxis
    )
}



@Composable
fun MonthlyLineChart(data: List<MonthlyTotal>) {
    if (data.isEmpty()) {
        Text("No data available")
        return
    }

    val entries = data.mapIndexed { index, item ->
        FloatEntry(index.toFloat(), item.total)
    }

    val model = entryModelOf(entries)

    val bottomAxis = rememberBottomAxis(
        valueFormatter = { value: Float, _: ChartValues ->
            val idx = value.toInt()
            data.getOrNull(idx)?.month ?: ""
        }
    )


    Chart(
        chart = lineChart(),
        model = model,
        startAxis = rememberStartAxis(),
        bottomAxis = bottomAxis
    )
}

// ----------------- DATE PICKER -----------------
@Composable
fun DateRangePicker(
    from: LocalDate?,
    to: LocalDate?,
    onFromSelected: (LocalDate) -> Unit,
    onToSelected: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = { onFromSelected(LocalDate.now().minusDays(30)) }) {
            Text("From: ${from ?: "Start"}")
        }
        TextButton(onClick = { onToSelected(LocalDate.now()) }) {
            Text("To: ${to ?: "End"}")
        }
    }
}
