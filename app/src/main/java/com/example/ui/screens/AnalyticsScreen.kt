package com.example.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.ui.theme.CategoryStyles
import com.example.ui.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expensesState.collectAsState()
    val budget by viewModel.budgetState.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdownState.collectAsState()
    val monthlySpending by viewModel.monthlySpendingState.collectAsState()
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    
    // Set Estimated Income based on global budget, allowing customized slider values
    var customIncome by remember(budget) { 
        mutableDoubleStateOf(if (budget > 0) budget else 2500.0) 
    }

    // Toggle for MoM (Month-over-Month) vs WoW (Week-over-Week) Trends
    var isMonthTrendMode by remember { mutableStateOf(true) }

    // --- WoW TREND CALCULATIONS ---
    val weeklyTrend = remember(expenses) {
        val oneDayMs = 24 * 60 * 60 * 1000L
        val oneWeekMs = 7 * oneDayMs
        val now = System.currentTimeMillis()
        
        (0..3).map { offset ->
            val startMs = now - (offset + 1) * oneWeekMs
            val endMs = now - offset * oneWeekMs
            
            val total = expenses.filter { exp ->
                exp.date in (startMs + 1)..endMs
            }.sumOf { it.amount }
            
            val label = when (offset) {
                0 -> "This Wk"
                1 -> "Last Wk"
                else -> "${offset}w Ago"
            }
            label to total
        }.reversed()
    }

    // --- MoM TREND CALCULATIONS ---
    val monthlyTrend = remember(expenses) {
        val cal = Calendar.getInstance()
        (0..4).map { offset ->
            val mCal = Calendar.getInstance()
            mCal.add(Calendar.MONTH, -offset)
            mCal.set(Calendar.DAY_OF_MONTH, 1)
            mCal.set(Calendar.HOUR_OF_DAY, 0)
            mCal.set(Calendar.MINUTE, 0)
            mCal.set(Calendar.SECOND, 0)
            mCal.set(Calendar.MILLISECOND, 0)
            mCal
        }.reversed().map { mCal ->
            val year = mCal.get(Calendar.YEAR)
            val month = mCal.get(Calendar.MONTH)
            
            val total = expenses.filter { exp ->
                val expCal = Calendar.getInstance().apply { timeInMillis = exp.date }
                expCal.get(Calendar.YEAR) == year && expCal.get(Calendar.MONTH) == month
            }.sumOf { it.amount }
            
            val label = SimpleDateFormat("MMM", Locale.US).format(mCal.time)
            label to total
        }
    }

    // --- SMART ANOMALY DETECTION ---
    val anomalies = remember(expenses) {
        val anomaliesList = mutableListOf<String>()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val oneWeekMs = 7 * oneDayMs
        val now = System.currentTimeMillis()
        
        val thisWeekStart = now - oneWeekMs
        val prior4WeeksStart = now - 5 * oneWeekMs
        
        val thisWeekExpenses = expenses.filter { it.date in (thisWeekStart + 1)..now }
        val priorExpenses = expenses.filter { it.date in (prior4WeeksStart + 1)..thisWeekStart }
        
        val categories = listOf("Food", "Transport", "Entertainment", "Bills", "Others")
        
        categories.forEach { category ->
            val thisWeekSpent = thisWeekExpenses.filter { it.category == category }.sumOf { it.amount }
            val priorTotal = priorExpenses.filter { it.category == category }.sumOf { it.amount }
            val priorWeeklyAvg = priorTotal / 4.0
            
            if (priorWeeklyAvg > 0.0) {
                val ratio = (thisWeekSpent - priorWeeklyAvg) / priorWeeklyAvg
                if (ratio >= 0.20 && (thisWeekSpent - priorWeeklyAvg) >= 15.0) {
                    val pctStr = String.format(Locale.US, "%.0f%%", ratio * 100)
                    val extraStr = currencyFormat.format(thisWeekSpent - priorWeeklyAvg)
                    anomaliesList.add("You've spent $pctStr ($extraStr) more on $category this week compared to your 4-week average.")
                }
            } else if (thisWeekSpent >= 40.0) {
                anomaliesList.add("Unusual Activity: You spent ${currencyFormat.format(thisWeekSpent)} on $category this week, which is a brand-new spending category.")
            }
        }
        anomaliesList
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, bottom = 12.dp, start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("analytics_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "SpendWise Insights",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Advanced spending curves and predictive diagnostics",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // 1. Time-Series Curves Card
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = "Curves",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Spending Trend Curves",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // Segmented Controls for Trend Type
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isMonthTrendMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { isMonthTrendMode = true }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("trend_mode_mom")
                                ) {
                                    Text(
                                        text = "MoM",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMonthTrendMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (!isMonthTrendMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { isMonthTrendMode = false }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("trend_mode_wow")
                                ) {
                                    Text(
                                        text = "WoW",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isMonthTrendMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Calculated trend slope percentage
                        val trendList = if (isMonthTrendMode) monthlyTrend else weeklyTrend
                        val currentVal = trendList.lastOrNull()?.second ?: 0.0
                        val priorVal = if (trendList.size >= 2) trendList[trendList.size - 2].second else 0.0
                        val slopePercent = if (priorVal > 0.0) ((currentVal - priorVal) / priorVal) * 100.0 else 0.0
                        val isUpward = slopePercent > 0.0
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = when {
                                    slopePercent > 1.0 -> Icons.Default.TrendingUp
                                    slopePercent < -1.0 -> Icons.Default.TrendingDown
                                    else -> Icons.Default.TrendingFlat
                                },
                                contentDescription = "Slope",
                                tint = when {
                                    slopePercent > 1.0 -> MaterialTheme.colorScheme.error
                                    slopePercent < -1.0 -> Color(0xFF10B981) // positive trend (spending down)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    slopePercent > 1.0 -> "Spending is curving upward by ${String.format(Locale.US, "%.1f%%", slopePercent)} this period."
                                    slopePercent < -1.0 -> "Spending is curving downward by ${String.format(Locale.US, "%.1f%%", -slopePercent)} this period."
                                    else -> "Spending trends are flat and stable."
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Custom Native Bezier Curve Canvas
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val strokeColor = MaterialTheme.colorScheme.primary
                        val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                        val textCol = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
                        val amtCol = MaterialTheme.colorScheme.primary.toArgb()

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .testTag("trend_canvas")
                        ) {
                            val maxAmount = (trendList.maxOfOrNull { it.second } ?: 100.0).coerceAtLeast(1.0) * 1.15
                            val topPad = 30.dp.toPx()
                            val botPad = 25.dp.toPx()
                            val usableHeight = size.height - topPad - botPad
                            val count = trendList.size

                            val path = Path()
                            val fillPath = Path()

                            if (count > 0) {
                                val firstX = if (count > 1) 0f else size.width / 2f
                                val firstY = size.height - botPad - ((trendList[0].second / maxAmount) * usableHeight).toFloat()
                                
                                path.moveTo(firstX, firstY)
                                fillPath.moveTo(firstX, firstY)

                                for (i in 1 until count) {
                                    val prevX = (i - 1).toFloat() / (count - 1) * size.width
                                    val prevY = size.height - botPad - ((trendList[i - 1].second / maxAmount) * usableHeight).toFloat()
                                    val currX = i.toFloat() / (count - 1) * size.width
                                    val currY = size.height - botPad - ((trendList[i].second / maxAmount) * usableHeight).toFloat()

                                    // Cubic Bezier spline control points
                                    val controlX1 = prevX + (currX - prevX) / 2f
                                    val controlY1 = prevY
                                    val controlX2 = prevX + (currX - prevX) / 2f
                                    val controlY2 = currY

                                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, currX, currY)
                                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, currX, currY)
                                }

                                if (count > 1) {
                                    fillPath.lineTo(size.width, size.height - botPad)
                                    fillPath.lineTo(0f, size.height - botPad)
                                    fillPath.close()

                                    // Draw background gradient fill under curve
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(primaryColor.copy(alpha = 0.22f), Color.Transparent),
                                            startY = topPad,
                                            endY = size.height - botPad
                                        )
                                    )
                                }

                                // Draw line curve
                                drawPath(
                                    path = path,
                                    color = strokeColor,
                                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Draw points & values labels
                                val textPaint = Paint().apply {
                                    color = textCol
                                    textSize = 10.dp.toPx()
                                    textAlign = Paint.Align.CENTER
                                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                }

                                val amtPaint = Paint().apply {
                                    color = amtCol
                                    textSize = 9.dp.toPx()
                                    textAlign = Paint.Align.CENTER
                                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                }

                                trendList.forEachIndexed { i, item ->
                                    val x = if (count > 1) i.toFloat() / (count - 1) * size.width else size.width / 2f
                                    val y = size.height - botPad - ((item.second / maxAmount) * usableHeight).toFloat()

                                    // Point dot
                                    drawCircle(color = surfaceVariantColor, radius = 5.dp.toPx(), center = Offset(x, y))
                                    drawCircle(color = strokeColor, radius = 3.dp.toPx(), center = Offset(x, y))

                                    // Amount string above point
                                    val amtStr = if (item.second >= 1000.0) {
                                        String.format(Locale.US, "$%.1fk", item.second / 1000.0)
                                    } else {
                                        String.format(Locale.US, "$%.0f", item.second)
                                    }
                                    drawContext.canvas.nativeCanvas.drawText(
                                        amtStr,
                                        x,
                                        y - 8.dp.toPx(),
                                        amtPaint
                                    )

                                    // Label string below point at axis base
                                    drawContext.canvas.nativeCanvas.drawText(
                                        item.first,
                                        x,
                                        size.height - 4.dp.toPx(),
                                        textPaint
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Peer-to-Peer Visual Comparison (Income vs. Stacked Expenses)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = "Income vs Expenses",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Income vs. Stacked Expenses",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Compare your monthly earnings against stacked category outlays to inspect your saving bandwidth.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Custom Stacked side-by-side Canvas bar chart
                        val emeraldGreen = Color(0xFF10B981)
                        val textColArgb = MaterialTheme.colorScheme.onSurface.toArgb()
                        val catLabelArgb = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
                        
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .testTag("stacked_bars_canvas")
                        ) {
                            val maxVal = maxOf(customIncome, monthlySpending).coerceAtLeast(1.0) * 1.15
                            val topPad = 25.dp.toPx()
                            val botPad = 25.dp.toPx()
                            val usableHeight = size.height - topPad - botPad
                            
                            val barWidth = 100f
                            val gap = 120f
                            val cx = size.width / 2f
                            
                            val leftBarX = cx - barWidth - gap / 2f
                            val rightBarX = cx + gap / 2f

                            // --- Draw Income Bar ---
                            val incomeHeight = ((customIncome / maxVal) * usableHeight).toFloat()
                            val incomeTop = size.height - botPad - incomeHeight
                            drawRoundRect(
                                color = emeraldGreen,
                                topLeft = Offset(leftBarX, incomeTop),
                                size = androidx.compose.ui.geometry.Size(barWidth, incomeHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                            )

                            // --- Draw Stacked Expenses Bar ---
                            var currentY = size.height - botPad
                            val categories = listOf("Food", "Transport", "Entertainment", "Bills", "Others")
                            
                            categories.forEachIndexed { idx, category ->
                                val spent = categoryBreakdown[category] ?: 0.0
                                if (spent > 0.0) {
                                    val segHeight = ((spent / maxVal) * usableHeight).toFloat()
                                    val style = CategoryStyles.getStyle(category)
                                    
                                    // Slight dividers between stacks
                                    drawRect(
                                        color = style.color,
                                        topLeft = Offset(rightBarX, currentY - segHeight),
                                        size = androidx.compose.ui.geometry.Size(barWidth, segHeight)
                                    )
                                    // Draw thin dividing line if not the bottom segment
                                    if (idx > 0) {
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(rightBarX, currentY),
                                            end = Offset(rightBarX + barWidth, currentY),
                                            strokeWidth = 1.5.dp.toPx()
                                        )
                                    }
                                    currentY -= segHeight
                                }
                            }
                            
                            // Draw top rounded cap outline or similar on right bar to look finished if there is spending
                            if (monthlySpending > 0) {
                                val totalExpHeight = ((monthlySpending / maxVal) * usableHeight).toFloat()
                                val expTop = size.height - botPad - totalExpHeight
                                // Just a simple indicator of total
                            }

                            // --- Draw Text Annotations inside Canvas ---
                            val labelPaint = Paint().apply {
                                color = catLabelArgb
                                textSize = 11.dp.toPx()
                                textAlign = Paint.Align.CENTER
                                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            }
                            val valPaint = Paint().apply {
                                color = textColArgb
                                textSize = 10.dp.toPx()
                                textAlign = Paint.Align.CENTER
                                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            }

                            // Left bar label/amount
                            drawContext.canvas.nativeCanvas.drawText(
                                "Income",
                                leftBarX + barWidth / 2f,
                                size.height - 4.dp.toPx(),
                                labelPaint
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                currencyFormat.format(customIncome),
                                leftBarX + barWidth / 2f,
                                incomeTop - 6.dp.toPx(),
                                valPaint
                            )

                            // Right bar label/amount
                            drawContext.canvas.nativeCanvas.drawText(
                                "Expenses",
                                rightBarX + barWidth / 2f,
                                size.height - 4.dp.toPx(),
                                labelPaint
                            )
                            val expTopY = size.height - botPad - ((monthlySpending / maxVal) * usableHeight).toFloat()
                            drawContext.canvas.nativeCanvas.drawText(
                                currencyFormat.format(monthlySpending),
                                rightBarX + barWidth / 2f,
                                expTopY - 6.dp.toPx(),
                                valPaint
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Color Legend for the Stacked bar
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val categories = listOf("Food", "Transport", "Entertainment", "Bills", "Others")
                            categories.forEach { category ->
                                val style = CategoryStyles.getStyle(category)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(style.color)
                                    )
                                    Text(
                                        text = category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Interactive estimated income control
                        Text(
                            text = "Estimated Monthly Income: ${currencyFormat.format(customIncome)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = customIncome.toFloat(),
                            onValueChange = { customIncome = it.toDouble() },
                            valueRange = 500f..10000f,
                            steps = 38, // steps of $250
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("income_slider")
                        )

                        // Spend ratio analysis details
                        val ratio = if (customIncome > 0.0) (monthlySpending / customIncome) * 100.0 else 0.0
                        val netSavings = customIncome - monthlySpending
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (ratio <= 80.0) emeraldGreen.copy(alpha = 0.06f)
                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.06f)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (ratio <= 80.0) "Healthy Spend Pattern" else "Elevated Spend Outlay",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (ratio <= 80.0) emeraldGreen else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (ratio <= 100.0) {
                                        "You are spending ${String.format(Locale.US, "%.0f%%", ratio)} of your income. You have ${currencyFormat.format(netSavings)} left for savings!"
                                    } else {
                                        "Warning: Your spending exceeds your customized income benchmark by ${String.format(Locale.US, "%.0f%%", ratio - 100.0)}!"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 3. Smart "Anomaly" Diagnostics Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Anomaly",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Smart Diagnostics & Anomalies",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (anomalies.isEmpty()) {
                            // Perfect state card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.08f))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success check",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(28.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "All Clear!",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                    Text(
                                        text = "No unusual spikes detected this week compared to your 4-week averages. Excellent budgeting consistency!",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            // Draw anomalies list
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                anomalies.forEach { msg ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Anomaly warn",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Text(
                                            text = msg,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
