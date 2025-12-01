package com.example.budgeting.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Analytics") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            Spacer(Modifier.height(8.dp))

            ModeSelector(selected = mode, onSelected = { viewModel.setMode(it) })

            Spacer(Modifier.height(8.dp))

            // --- FILTER ROW WITH CATEGORY AND CALENDAR DATE PICKERS ---
            AnalyticsFilterRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setCategory(it) },
                from = from,
                to = to,
                onFromSelected = { viewModel.setDateFrom(it) },
                onToSelected = { viewModel.setDateTo(it) }
            )

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
                else -> AnalyticsContent(categoryCounts, categoryAmounts, monthlyTotals)
            }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // --- CATEGORY BUTTON WITH ICON ---
        Box {
            IconButton(onClick = { showCategoryMenu = true }) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = "Select Category",
                    tint = if (selectedCategory != "All")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = showCategoryMenu,
                onDismissRequest = { showCategoryMenu = false }
            ) {
                DropdownMenuItem(text = { Text("All") }, onClick = {
                    onCategorySelected("All")
                    showCategoryMenu = false
                })
                categories.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat) }, onClick = {
                        onCategorySelected(cat)
                        showCategoryMenu = false
                    })
                }
            }
        }

        // --- DATE PICKERS WITH ICONS ---
        Row {
            // From Date
            IconButton(onClick = { showFromDatePicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Select From Date")
            }
            TextButton(onClick = { showFromDatePicker = true }) {
                Text(from?.toString() ?: "From")
            }

            Spacer(Modifier.width(8.dp))

            // To Date
            IconButton(onClick = { showToDatePicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Select To Date")
            }
            TextButton(onClick = { showToDatePicker = true }) {
                Text(to?.toString() ?: "To")
            }
        }
    }

    // --- FROM DATE PICKER DIALOG ---
    if (showFromDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = from?.toEpochDay()?.times(86400000L)
        )
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onFromSelected(LocalDate.ofEpochDay(millis / 86400000L))
                    }
                    showFromDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    // --- TO DATE PICKER DIALOG ---
    if (showToDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = to?.toEpochDay()?.times(86400000L)
        )
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onToSelected(LocalDate.ofEpochDay(millis / 86400000L))
                    }
                    showToDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

// ----------------- REMAINING ANALYTICS CHARTS -----------------
@Composable
fun AnalyticsContent(
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

// ----------------- CATEGORY DROPDOWN -----------------
@Composable
fun CategoryDropdown(
    categories: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text("Category: ${selected}")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(text = { Text("All") }, onClick = {
                onSelected("All")
                expanded = false
            })

            categories.forEach { cat ->
                DropdownMenuItem(text = { Text(cat) }, onClick = {
                    onSelected(cat)
                    expanded = false
                })
            }
        }
    }
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
