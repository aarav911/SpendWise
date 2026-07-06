package com.example.ui.components

import androidx.compose.runtime.withFrameMillis
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavingsGoal
import java.text.NumberFormat
import java.util.Locale
import java.util.Random

@Composable
fun SavingsGoalsTracker(
    goals: List<SavingsGoal>,
    onAddGoalClick: () -> Unit,
    onTransferClick: (SavingsGoal) -> Unit,
    onDeleteClick: (SavingsGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = "Savings Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Savings Goals & Wallets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onAddGoalClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("add_goal_trigger")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Goal",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (goals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .clickable { onAddGoalClick() }
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Piggybank",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Create a savings goal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Save for things like a 'New Laptop' or 'Vacation' by transferring leftovers.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    goals.forEach { goal ->
                        val ratio = if (goal.targetAmount > 0.0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
                        val progress = ratio.coerceIn(0f, 1f)
                        val isFinished = goal.currentAmount >= goal.targetAmount

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isFinished) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (isFinished) 1.dp else 0.dp,
                                    color = if (isFinished) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(if (isFinished) 12.dp else 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isFinished) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completed Goal",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = CircleShape,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Savings,
                                                    contentDescription = "Active Goal",
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = goal.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFinished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isFinished) {
                                        IconButton(
                                            onClick = { onTransferClick(goal) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("transfer_button_${goal.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Transfer Funds",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteClick(goal) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("delete_goal_${goal.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Goal",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${currencyFormat.format(goal.currentAmount)} saved of ${currencyFormat.format(goal.targetAmount)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.US, "%.0f%%", progress * 100f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFinished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                color = if (isFinished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float = 1.0f
)

@Composable
fun CelebrationConfetti(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    val particles = remember { mutableStateListOf<ConfettiParticle>() }
    val colors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFFF4500), // OrangeRed
        Color(0xFF32CD32), // LimeGreen
        Color(0xFF1E90FF), // DodgerBlue
        Color(0xFFFF69B4), // HotPink
        Color(0xFF8A2BE2)  // BlueViolet
    )

    LaunchedEffect(Unit) {
        val random = Random()
        for (i in 0 until 120) {
            val angle = random.nextFloat() * 2 * Math.PI
            val speed = 6f + random.nextFloat() * 22f
            particles.add(
                ConfettiParticle(
                    x = 540f,
                    y = 960f,
                    vx = (Math.cos(angle) * speed).toFloat(),
                    vy = (Math.sin(angle) * speed).toFloat() - 4f,
                    color = colors[random.nextInt(colors.size)],
                    size = 8f + random.nextFloat() * 14f
                )
            )
        }

        val startTime = withFrameMillis { it }
        var lastTime = startTime
        while (particles.isNotEmpty()) {
            val currentTime = withFrameMillis { it }
            val dt = (currentTime - lastTime) / 16.6f
            lastTime = currentTime

            for (i in particles.indices.reversed()) {
                val p = particles[i]
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.vy += 0.4f * dt
                p.alpha -= 0.012f * dt

                if (p.alpha <= 0f) {
                    particles.removeAt(i)
                }
            }
        }
        onFinished()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        particles.forEach { p ->
            if (p.x == 540f && p.y == 960f) {
                p.x = cx
                p.y = cy
            }

            drawCircle(
                color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                radius = p.size,
                center = Offset(p.x, p.y)
            )
        }
    }
}
