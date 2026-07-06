package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetProgressBar(
    spent: Double,
    budget: Double,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    
    val ratio = if (budget > 0.0) (spent / budget).toFloat() else 0f
    val progress = ratio.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "budget_progress"
    )

    val isExceeded = spent > budget
    val isNearLimit = spent >= budget * 0.85 && spent <= budget
    
    val indicatorColor by animateColorAsState(
        targetValue = when {
            isExceeded -> MaterialTheme.colorScheme.error
            isNearLimit -> Color(0xFFE65100) // Orange warning
            else -> MaterialTheme.colorScheme.primary
        },
        label = "indicator_color"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Monthly Budget Tracker",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${currencyFormat.format(spent)} spent of ${currencyFormat.format(budget)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            
            val remaining = budget - spent
            Surface(
                color = when {
                    isExceeded -> MaterialTheme.colorScheme.errorContainer
                    isNearLimit -> Color(0xFFFFF3E0)
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Text(
                    text = if (isExceeded) {
                        "Over by ${currencyFormat.format(-remaining)}"
                    } else {
                        "${currencyFormat.format(remaining)} left"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isExceeded -> MaterialTheme.colorScheme.onErrorContainer
                        isNearLimit -> Color(0xFFE65100)
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            color = indicatorColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        )

        if (isExceeded) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "You have exceeded your monthly budget limit!",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
