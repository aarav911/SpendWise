package com.example.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryStyle(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

object CategoryStyles {
    val list = listOf(
        CategoryStyle("Food", Icons.Default.Fastfood, Color(0xFF6366F1)),          // Sleek Indigo
        CategoryStyle("Transport", Icons.Default.DirectionsCar, Color(0xFF0EA5E9)), // Ocean Blue
        CategoryStyle("Entertainment", Icons.Default.ConfirmationNumber, Color(0xFFF59E0B)), // Vivid Amber
        CategoryStyle("Bills", Icons.AutoMirrored.Filled.ReceiptLong, Color(0xFFEF4444)),      // Coral Red
        CategoryStyle("Others", Icons.Default.Category, Color(0xFF8B5CF6))          // Violet Purple
    )

    fun getStyle(category: String): CategoryStyle {
        return list.find { it.name.equals(category, ignoreCase = true) }
            ?: CategoryStyle(category, Icons.Default.Category, Color(0xFF37474F)) // Slate fallback
    }
}
