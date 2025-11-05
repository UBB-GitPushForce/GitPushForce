package com.example.budgeting.android

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF6C63FF),
            onPrimary = Color(0xFFFFFFFF),
            background = Color(0xFF0A0A23),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF111133),
            onSurface = Color(0xFFE6E6E6),
            surfaceVariant = Color(0xFF1B1B3A),
            onSurfaceVariant = Color(0xFFB3B3B3),
            error = Color(0xFFCF6679),
            outline = Color(0xFF6F6F6F)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6C63FF),
            onPrimary = Color(0xFFFFFFFF),
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF0A0A23),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0A0A23),
            surfaceVariant = Color(0xFFF2F2F7),
            onSurfaceVariant = Color(0xFF6E6E73),
            error = Color(0xFFB00020),
            outline = Color(0xFFBDBDBD)
        )
    }
    val typography = Typography(
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
    )
    val shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(0.dp)
    )

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
