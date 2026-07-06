package com.example.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Asset
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assets by viewModel.assetsState.collectAsState()
    val rawTimeline by viewModel.portfolioTimelineState.collectAsState()
    val monthlyInvestmentTotal by viewModel.monthlyInvestmentTotalState.collectAsState()
    val budget by viewModel.budgetState.collectAsState()
    val scope = rememberCoroutineScope()

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    
    // Modern custom income slider (default to budget or $3000 if budget is not set)
    var customIncome by remember(budget) { 
        mutableStateOf(if (budget > 0) budget else 3000.0) 
    }

    // --- TIMELINE FILTERING ---
    var selectedInterval by remember { mutableStateOf("ALL") }
    val filteredTimeline = remember(rawTimeline, selectedInterval) {
        val now = System.currentTimeMillis()
        val durationMs = when (selectedInterval) {
            "1W" -> 7 * 24 * 60 * 60 * 1000L
            "1M" -> 30L * 24 * 60 * 60 * 1000L
            "6M" -> 180L * 24 * 60 * 60 * 1000L
            "1Y" -> 365L * 24 * 60 * 60 * 1000L
            else -> Long.MAX_VALUE
        }
        val points = if (selectedInterval == "ALL") {
            rawTimeline
        } else {
            rawTimeline.filter { it.first >= (now - durationMs) }
        }
        
        // Ensure we always have some data to display
        if (points.size >= 2) points else rawTimeline
    }

    // --- PORTFOLIO METRICS ---
    val totalCurrentValue = remember(assets) {
        assets.sumOf { it.quantity * it.currentPrice }
    }
    val totalCostBasis = remember(assets) {
        assets.sumOf { it.costBasis }
    }
    val totalReturnAmount = totalCurrentValue - totalCostBasis
    val totalReturnPercent = if (totalCostBasis > 0.0) (totalReturnAmount / totalCostBasis) * 100.0 else 0.0

    // --- CATEGORY BREAKDOWNS ---
    val categories = listOf("Stocks/Mutual Funds", "Crypto", "Physical Gold/Commodities", "Dwelling/Real Estate", "Intellectual Capital")
    val categoryColors = remember {
        mapOf(
            "Stocks/Mutual Funds" to Color(0xFF0284C7),       // Ocean Blue
            "Crypto" to Color(0xFF7C3AED),                    // Violet
            "Physical Gold/Commodities" to Color(0xFFD97706), // Amber
            "Dwelling/Real Estate" to Color(0xFF4F46E5),      // Indigo
            "Intellectual Capital" to Color(0xFF64748B)       // Muted Slate
        )
    }

    val assetAllocation = remember(assets) {
        val distribution = mutableMapOf<String, Double>()
        categories.forEach { dist -> distribution[dist] = 0.0 }
        assets.forEach { asset ->
            val current = distribution[asset.category] ?: 0.0
            distribution[asset.category] = current + (asset.quantity * asset.currentPrice)
        }
        distribution
    }

    // Modal/Dialog state
    var showTransactionSheet by remember { mutableStateOf(false) }

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
                        modifier = Modifier.testTag("wealth_back_button")
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
                            text = "Wealth Portfolio",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Track manual asset allocation and compounding growth offline",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showTransactionSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Manage Assets") },
                text = { Text("Log Transaction") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("wealth_fab")
                    .padding(bottom = 16.dp, end = 8.dp)
            )
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
            
            // Section A: The Legacy Banner (Aggregate Value & First Tenth Progress)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                text = "Thy Accumulated Treasure",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currencyFormat.format(totalCurrentValue),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("total_portfolio_value")
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Delta Badge
                            val isPositive = totalReturnAmount >= 0.0
                            val badgeColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                            val badgeIcon = if (isPositive) "▲" else "▼"
                            val plusSign = if (isPositive) "+" else ""
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(badgeColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$plusSign${currencyFormat.format(totalReturnAmount)} ($badgeIcon ${String.format(Locale.US, "%.1f%%", totalReturnPercent)})",
                                    color = badgeColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Circular Progress Ring: Pay Yourself First (at least 10% of customIncome)
                        val payFirstTarget = customIncome * 0.10
                        val progressPercent = if (payFirstTarget > 0.0) {
                            (monthlyInvestmentTotal / payFirstTarget).coerceIn(0.0, 1.0)
                        } else 0.0
                        val ringActiveColor = if (progressPercent >= 1.0) Color(0xFF4F46E5) else Color(0xFF10B981)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawArc(
                                        color = Color.LightGray.copy(alpha = 0.3f),
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = ringActiveColor,
                                        startAngle = -90f,
                                        sweepAngle = (progressPercent * 360f).toFloat(),
                                        useCenter = false,
                                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format(Locale.US, "%.0f%%", progressPercent * 100),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ringActiveColor
                                    )
                                    Text(
                                        text = "Saved",
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Monthly 10% Goal",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Section B: Growth Timeline (Bezier curve chart with gestures)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = "Growth Curve",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Portfolio Growth Curve",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Dynamic Interval Selection Chips
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                    .padding(2.dp)
                            ) {
                                listOf("1W", "1M", "ALL").forEach { interval ->
                                    val isSelected = selectedInterval == interval
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable { selectedInterval = interval }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = interval,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (filteredTimeline.isEmpty()) {
                            // Minimalist mock timeline display when empty
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No growth records yet. Log your first asset below!",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            var activeTouchX by remember { mutableStateOf<Float?>(null) }
                            val strokeColor = MaterialTheme.colorScheme.primary
                            val textCol = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
                            val primaryCol = MaterialTheme.colorScheme.primary
                            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .pointerInput(filteredTimeline) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    val anyPressed = event.changes.any { it.pressed }
                                                    if (anyPressed) {
                                                        val pos = event.changes.firstOrNull()?.position
                                                        if (pos != null) {
                                                            activeTouchX = pos.x
                                                        }
                                                    } else {
                                                        activeTouchX = null
                                                    }
                                                }
                                            }
                                        }
                                        .testTag("growth_timeline_canvas")
                                ) {
                                    val topPad = 30.dp.toPx()
                                    val botPad = 25.dp.toPx()
                                    val usableHeight = size.height - topPad - botPad
                                    val count = filteredTimeline.size

                                    val maxVal = (filteredTimeline.maxOf { it.second }).coerceAtLeast(10.0) * 1.15
                                    val minVal = (filteredTimeline.minOf { it.second }).coerceAtBeforeMax(maxVal)

                                    val path = Path()
                                    val fillPath = Path()

                                    val pointsList = filteredTimeline.mapIndexed { i, item ->
                                        val x = if (count > 1) i.toFloat() / (count - 1) * size.width else size.width / 2f
                                        val valueRange = if (maxVal > minVal) maxVal - minVal else 1.0
                                        val y = size.height - botPad - (((item.second - minVal) / valueRange) * usableHeight).toFloat()
                                        Offset(x, y)
                                    }

                                    if (pointsList.isNotEmpty()) {
                                        val first = pointsList.first()
                                        path.moveTo(first.x, first.y)
                                        fillPath.moveTo(first.x, first.y)

                                        for (i in 1 until pointsList.size) {
                                            val prev = pointsList[i - 1]
                                            val curr = pointsList[i]

                                            val controlX1 = prev.x + (curr.x - prev.x) / 2f
                                            val controlY1 = prev.y
                                            val controlX2 = prev.x + (curr.x - prev.x) / 2f
                                            val controlY2 = curr.y

                                            path.cubicTo(controlX1, controlY1, controlX2, controlY2, curr.x, curr.y)
                                            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, curr.x, curr.y)
                                        }

                                        if (pointsList.size > 1) {
                                            fillPath.lineTo(size.width, size.height - botPad)
                                            fillPath.lineTo(0f, size.height - botPad)
                                            fillPath.close()

                                            drawPath(
                                                path = fillPath,
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(primaryCol.copy(alpha = 0.22f), Color.Transparent),
                                                    startY = topPad,
                                                    endY = size.height - botPad
                                                )
                                            )
                                        }

                                        drawPath(
                                            path = path,
                                            color = strokeColor,
                                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                                        )

                                        // Draw date labels on bottom
                                        val textPaint = Paint().apply {
                                            color = textCol
                                            textSize = 8.dp.toPx()
                                            textAlign = Paint.Align.CENTER
                                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                        }

                                        if (count > 1) {
                                            val sdf = SimpleDateFormat("MM/dd", Locale.US)
                                            // Draw labels for first, middle, and last points
                                            val labelIndices = listOf(0, count / 2, count - 1).distinct()
                                            labelIndices.forEach { idx ->
                                                val point = pointsList[idx]
                                                val dateStr = sdf.format(Date(filteredTimeline[idx].first))
                                                drawContext.canvas.nativeCanvas.drawText(
                                                    dateStr,
                                                    point.x.coerceIn(20f, size.width - 20f),
                                                    size.height - 4.dp.toPx(),
                                                    textPaint
                                                )
                                            }
                                        }

                                        // --- TOOLTIP RENDERING ON DRAG / TOUCH ---
                                        val currentTouchX = activeTouchX
                                        if (currentTouchX != null && count > 0) {
                                            val stepWidth = if (count > 1) size.width / (count - 1) else size.width
                                            val closestIdx = ((currentTouchX / stepWidth) + 0.5f).toInt().coerceIn(0, count - 1)
                                            val selectedPoint = pointsList[closestIdx]
                                            val selectedData = filteredTimeline[closestIdx]

                                            // Draw vertical dotted guide-rule line
                                            drawLine(
                                                color = strokeColor.copy(alpha = 0.4f),
                                                start = Offset(selectedPoint.x, topPad),
                                                end = Offset(selectedPoint.x, size.height - botPad),
                                                strokeWidth = 1.5.dp.toPx()
                                            )

                                            // Draw point dot highlight
                                            drawCircle(color = surfaceVariantColor, radius = 7.dp.toPx(), center = selectedPoint)
                                            drawCircle(color = strokeColor, radius = 4.dp.toPx(), center = selectedPoint)

                                            // Draw floating tooltip
                                            val formattedVal = currencyFormat.format(selectedData.second)
                                            val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(selectedData.first))
                                            val tooltipText = "$formattedDate: $formattedVal"

                                            val tooltipPaint = Paint().apply {
                                                color = Color.White.toArgb()
                                                textSize = 10.dp.toPx()
                                                textAlign = Paint.Align.CENTER
                                                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                            }

                                            val rectPaint = Paint().apply {
                                                color = Color(0xFF1E293B).toArgb() // Slate Dark
                                                style = Paint.Style.FILL
                                            }

                                            val textWidth = tooltipPaint.measureText(tooltipText)
                                            val padX = 8.dp.toPx()
                                            val padY = 6.dp.toPx()
                                            val boxHeight = 24.dp.toPx()

                                            val rectLeft = (selectedPoint.x - textWidth / 2f - padX).coerceIn(4f, size.width - textWidth - padX * 2f - 4f)
                                            val rectRight = rectLeft + textWidth + padX * 2f
                                            val rectTop = (selectedPoint.y - boxHeight - 12.dp.toPx()).coerceAtLeast(4f)
                                            val rectBot = rectTop + boxHeight

                                            drawContext.canvas.nativeCanvas.drawRoundRect(
                                                rectLeft, rectTop, rectRight, rectBot,
                                                6.dp.toPx(), 6.dp.toPx(), rectPaint
                                            )

                                            drawContext.canvas.nativeCanvas.drawText(
                                                tooltipText,
                                                rectLeft + (rectRight - rectLeft) / 2f,
                                                rectTop + boxHeight / 2f + 3.dp.toPx(),
                                                tooltipPaint
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section C: Doughnut Chart Asset Allocation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = "Asset Distribution",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Asset Class Allocation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (totalCurrentValue <= 0.0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No allocations to display. Log an asset to build your wealth chart.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Doughnut Chart Canvas
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(130.dp)
                                        .weight(1.1f)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        var currentStartAngle = -90f
                                        categories.forEach { category ->
                                            val categoryVal = assetAllocation[category] ?: 0.0
                                            if (categoryVal > 0.0) {
                                                val sweepAngle = ((categoryVal / totalCurrentValue) * 360f).toFloat()
                                                val color = categoryColors[category] ?: Color.Gray
                                                drawArc(
                                                    color = color,
                                                    startAngle = currentStartAngle,
                                                    sweepAngle = sweepAngle,
                                                    useCenter = false,
                                                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Butt)
                                                )
                                                currentStartAngle += sweepAngle
                                            }
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Portfolio Icon",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Legends
                                Column(
                                    modifier = Modifier.weight(1.5f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    categories.forEach { category ->
                                        val categoryVal = assetAllocation[category] ?: 0.0
                                        val ratioPercent = if (totalCurrentValue > 0.0) (categoryVal / totalCurrentValue) * 100.0 else 0.0
                                        
                                        // "Guard Thy Treasure" Safety Alert: If high-risk allocation class > 70%,
                                        // shift color organically to Warm Orange (0xFFEA580C)
                                        val isAlertActive = ratioPercent > 70.0
                                        val labelColor = if (isAlertActive) Color(0xFFEA580C) else MaterialTheme.colorScheme.onSurfaceVariant
                                        val swatchColor = if (isAlertActive) Color(0xFFEA580C) else (categoryColors[category] ?: Color.Gray)

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(swatchColor)
                                            )
                                            Column {
                                                Text(
                                                    text = category,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isAlertActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    color = labelColor,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${currencyFormat.format(categoryVal)} (${String.format(Locale.US, "%.1f%%", ratioPercent)})",
                                                    fontSize = 10.sp,
                                                    color = if (isAlertActive) Color(0xFFEA580C).copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    fontWeight = if (isAlertActive) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Interactive Income Configuration Slider (Fatten Thy Purse target setup)
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Reference Monthly Income: ${currencyFormat.format(customIncome)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Adjust to evaluate monthly savings targets (10% standard savings goal is ${currencyFormat.format(customIncome * 0.1)})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = customIncome.toFloat(),
                            onValueChange = { customIncome = it.toDouble() },
                            valueRange = 1000f..15000f,
                            steps = 28,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wealth_income_slider")
                        )
                    }
                }
            }

            // Section D: Ledger & Appraisals
            item {
                Text(
                    text = "Asset Ledger & Appraisals",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (assets.isEmpty() || assets.all { it.quantity <= 0 }) {
                item {
                    // Modern Graceful Empty State
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "🏛️",
                                fontSize = 36.sp
                            )
                            Text(
                                text = "Your Purse is Currently Lean",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "A part of all you earn is yours to keep. Tap the '+' button below to log your first asset purchase, put your savings to work, and watch your wealth timeline begin.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(assets.filter { it.quantity > 0 }) { asset ->
                    val assetValue = asset.quantity * asset.currentPrice
                    val assetReturn = assetValue - asset.costBasis
                    val assetReturnPercent = if (asset.costBasis > 0.0) (assetReturn / asset.costBasis) * 100.0 else 0.0
                    val isAssetReturnPositive = assetReturn >= 0.0
                    val returnColor = if (isAssetReturnPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                    val returnIcon = if (isAssetReturnPositive) "▲" else "▼"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("asset_card_${asset.name}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(categoryColors[asset.category] ?: Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = asset.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Holdings: ${String.format(Locale.US, "%.4f", asset.quantity)} @ ${currencyFormat.format(asset.currentPrice)}/unit",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Cost Basis: ${currencyFormat.format(asset.costBasis)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = currencyFormat.format(assetValue),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(returnColor.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "$returnIcon ${String.format(Locale.US, "%.1f%%", assetReturnPercent)}",
                                        color = returnColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    // Modern Dialog Sheet for Transaction Execution
    if (showTransactionSheet) {
        var actionType by remember { mutableStateOf("BUY") } // BUY, SELL, APPRAISE
        var assetName by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf(categories.first()) }
        var inputQuantity by remember { mutableStateOf("") }
        var inputPrice by remember { mutableStateOf("") }
        
        // Appraisal Asset selection
        var selectedAssetToAppraise by remember { mutableStateOf<Asset?>(null) }
        var selectedAssetToSell by remember { mutableStateOf<Asset?>(null) }

        // Safeguard triggers
        var hasSufficientQuantityError by remember { mutableStateOf(false) }
        var validationErrorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showTransactionSheet = false },
            title = {
                Text(
                    text = when (actionType) {
                        "BUY" -> "Log Asset Acquisition"
                        "SELL" -> "Log Asset Disposition"
                        else -> "Manual Value Appraisal"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Action Tab Picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                            .padding(2.dp)
                    ) {
                        listOf("BUY", "SELL", "APPRAISE").forEach { type ->
                            val active = actionType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        actionType = type
                                        hasSufficientQuantityError = false
                                        validationErrorMsg = ""
                                        // Prefill first item if Appraisal or Sell is chosen
                                        if (type == "APPRAISE" && assets.isNotEmpty()) {
                                            val firstWithQty = assets.firstOrNull { it.quantity > 0 } ?: assets.first()
                                            selectedAssetToAppraise = firstWithQty
                                            inputPrice = firstWithQty.currentPrice.toString()
                                        } else if (type == "SELL" && assets.isNotEmpty()) {
                                            val firstWithQty = assets.firstOrNull { it.quantity > 0 } ?: assets.first()
                                            selectedAssetToSell = firstWithQty
                                            inputPrice = firstWithQty.currentPrice.toString()
                                            inputQuantity = ""
                                        } else {
                                            assetName = ""
                                            inputQuantity = ""
                                            inputPrice = ""
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    if (actionType == "APPRAISE") {
                        if (assets.isEmpty()) {
                            Text(
                                text = "You must own assets to perform an appraisal.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        } else {
                            // Dropdown/Selector for Assets
                            Text(
                                text = "Select Asset to Appraise",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            var expandedAppraisalAssets by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { expandedAppraisalAssets = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                    modifier = Modifier.fillMaxWidth().testTag("appraise_select_button")
                                ) {
                                    Text(selectedAssetToAppraise?.name ?: "Select Asset")
                                }
                                DropdownMenu(
                                    expanded = expandedAppraisalAssets,
                                    onDismissRequest = { expandedAppraisalAssets = false }
                                ) {
                                    assets.forEach { asset ->
                                        DropdownMenuItem(
                                            text = { Text("${asset.name} (${asset.category})") },
                                            onClick = {
                                                selectedAssetToAppraise = asset
                                                inputPrice = asset.currentPrice.toString()
                                                expandedAppraisalAssets = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = inputPrice,
                                onValueChange = { inputPrice = it },
                                label = { Text("New Market Price Per Unit") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("appraise_price_input")
                            )
                        }
                    } else if (actionType == "SELL") {
                        if (assets.none { it.quantity > 0 }) {
                            Text(
                                text = "You have no active asset balances to sell.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        } else {
                            // Dropdown/Selector for Assets to Sell
                            Text(
                                text = "Select Asset to Sell",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            var expandedSellAssets by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { expandedSellAssets = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                    modifier = Modifier.fillMaxWidth().testTag("sell_select_button")
                                ) {
                                    Text(selectedAssetToSell?.name ?: "Select Asset")
                                }
                                DropdownMenu(
                                    expanded = expandedSellAssets,
                                    onDismissRequest = { expandedSellAssets = false }
                                ) {
                                    assets.filter { it.quantity > 0 }.forEach { asset ->
                                        DropdownMenuItem(
                                            text = { Text("${asset.name} (Holdings: ${asset.quantity})") },
                                            onClick = {
                                                selectedAssetToSell = asset
                                                inputPrice = asset.currentPrice.toString()
                                                expandedSellAssets = false
                                                hasSufficientQuantityError = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = inputQuantity,
                                onValueChange = { qty ->
                                    inputQuantity = qty
                                    val qDouble = qty.toDoubleOrNull() ?: 0.0
                                    val available = selectedAssetToSell?.quantity ?: 0.0
                                    hasSufficientQuantityError = qDouble > available
                                },
                                label = { Text("Quantity to Sell") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = hasSufficientQuantityError,
                                colors = OutlinedTextFieldDefaults.colors(
                                    errorBorderColor = Color(0xFFEF4444)
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("sell_quantity_input")
                            )

                            if (hasSufficientQuantityError) {
                                Text(
                                    text = "Error: Input exceeds available balance (${selectedAssetToSell?.quantity ?: 0.0})",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedTextField(
                                value = inputPrice,
                                onValueChange = { inputPrice = it },
                                label = { Text("Sale Price Per Unit") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("sell_price_input")
                            )
                        }
                    } else {
                        // BUY transaction
                        OutlinedTextField(
                            value = assetName,
                            onValueChange = { if (it.length <= 50) assetName = it },
                            label = { Text("Asset Ticker or Name") },
                            placeholder = { Text("e.g. BTC, VTI, Gold") },
                            modifier = Modifier.fillMaxWidth().testTag("buy_name_input"),
                            supportingText = { Text("${assetName.length}/50") }
                        )

                        Text(
                            text = "Asset Class Category",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        var expandedCategories by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { expandedCategories = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.fillMaxWidth().testTag("buy_category_button")
                            ) {
                                Text(selectedCategory)
                            }
                            DropdownMenu(
                                expanded = expandedCategories,
                                onDismissRequest = { expandedCategories = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            selectedCategory = cat
                                            expandedCategories = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = inputQuantity,
                            onValueChange = { inputQuantity = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("buy_quantity_input")
                        )

                        OutlinedTextField(
                            value = inputPrice,
                            onValueChange = { inputPrice = it },
                            label = { Text("Acquisition Price Per Unit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("buy_price_input")
                        )
                    }

                    if (validationErrorMsg.isNotEmpty()) {
                        Text(
                            text = validationErrorMsg,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = inputQuantity.toDoubleOrNull() ?: 0.0
                        val prc = inputPrice.toDoubleOrNull() ?: 0.0
                        
                        if (actionType == "APPRAISE") {
                            val asset = selectedAssetToAppraise
                            if (asset == null) {
                                validationErrorMsg = "Select an asset to appraisal."
                                return@Button
                            }
                            if (prc <= 0.0) {
                                validationErrorMsg = "Price must be positive and non-zero."
                                return@Button
                            }
                            viewModel.updateAssetPrice(asset.id, prc)
                            showTransactionSheet = false
                        } else if (actionType == "SELL") {
                            val asset = selectedAssetToSell
                            if (asset == null) {
                                validationErrorMsg = "Select an asset to sell."
                                return@Button
                            }
                            if (qty <= 0.0 || prc <= 0.0) {
                                validationErrorMsg = "Quantity and Price must be positive non-zero values."
                                return@Button
                            }
                            if (qty > asset.quantity) {
                                validationErrorMsg = "Cannot sell more than available holdings."
                                return@Button
                            }
                            scope.launch {
                                val success = viewModel.sellAsset(asset.name, asset.category, qty, prc)
                                if (success) {
                                    showTransactionSheet = false
                                } else {
                                    validationErrorMsg = "Transaction failed due to safeguard lock."
                                }
                            }
                        } else {
                            // BUY
                            val name = assetName.trim()
                            if (name.isEmpty()) {
                                validationErrorMsg = "Asset Name cannot be empty."
                                return@Button
                            }
                            if (qty <= 0.0 || prc <= 0.0) {
                                validationErrorMsg = "Quantity and Price must be positive non-zero values."
                                return@Button
                            }
                            viewModel.buyAsset(name, selectedCategory, qty, prc)
                            showTransactionSheet = false
                        }
                    },
                    modifier = Modifier.testTag("dialog_confirm_button")
                ) {
                    Text("Execute")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransactionSheet = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Utility extension function to prevent minVal equal to maxVal causing dividing by zero crash
private fun Double.coerceAtBeforeMax(max: Double): Double {
    return if (this >= max) max * 0.9 else this
}
