package com.umair.exactpic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MonochromeColorScheme =
  darkColorScheme(
    primary = AppColors.ActivePillBackground,
    onPrimary = AppColors.ActivePillText,
    primaryContainer = AppColors.DarkPillBackground,
    onPrimaryContainer = AppColors.TextPrimary,
    secondary = AppColors.ActivePillBackground,
    onSecondary = AppColors.ActivePillText,
    secondaryContainer = AppColors.DarkPillBackground,
    onSecondaryContainer = AppColors.TextPrimary,
    tertiary = AppColors.ActivePillBackground,
    onTertiary = AppColors.ActivePillText,
    tertiaryContainer = AppColors.DarkPillBackground,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.CardBackground,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.CardElevated,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.CardBorder,
    outlineVariant = AppColors.CardBorderLight
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = MonochromeColorScheme,
    typography = Typography,
    content = content
  )
}

