// HelperViews.kt
package com.example.gogomarket.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row // <-- ИЗМЕНЕНИЕ: импортируем Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DetailRow(icon: ImageVector, title: String, value: String) {
    Row(verticalAlignment = Alignment.Top) { // <-- ИЗМЕНЕНИЕ: HStack заменен на Row
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.width(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp)) // Добавили отступ для красоты
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}