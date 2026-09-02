package com.umair.exactpic.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Single Color Constant File for the entire application.
 * Strictly adheres to the monochrome high-contrast dark aesthetic shown in the reference screens.
 * Contains no blues, cyans, or vibrant hues.
 */
object AppColors {
    // Canvas & Main Backgrounds
    val PureBlack = Color(0xFF000000)
    val Background = Color(0xFF000000)
    val BackgroundDark = Color(0xFF000000)
    val SurfaceDark = Color(0xFF000000)
    val TopBarBackground = Color(0xFF000000)

    // Cards & Containers
    val CardBackground = Color(0xFF141414)
    val CardElevated = Color(0xFF191919)
    val CardBorder = Color(0xFF222222)
    val CardBorderLight = Color(0xFF2C2C2E)
    val CircleIconBackground = Color(0xFF1F1F1F)
    val Divider = Color(0xFF242424)

    // Inputs & Edit Boxes
    val InputBackground = Color(0xFF1A1A1A)
    val InputBorder = Color(0xFF2C2C2E)
    val InputText = Color(0xFFFFFFFF)
    val InputTrailingText = Color(0xFF8E8E93)

    // Dark Buttons & Unselected Chips
    val DarkPillBackground = Color(0xFF1A1A1A)
    val DarkPillBorder = Color(0xFF2E2E32)
    val DarkPillText = Color(0xFFFFFFFF)

    // Active Highlights (High-contrast pure white with black text)
    val ActivePillBackground = Color(0xFFFFFFFF)
    val ActivePillText = Color(0xFF000000)

    val ActionButtonBackground = Color(0xFFFFFFFF)
    val ActionButtonText = Color(0xFF000000)
    val ResetButtonBackground = Color(0xFF1A1A1A)
    val ResetButtonText = Color(0xFFFFFFFF)

    // Segmented Dimension Unit Bar (Pixels, Inches, Cm)
    val SegmentContainer = Color(0xFF161618)
    val SegmentSelected = Color(0xFF2C2C2E)
    val SegmentUnselectedText = Color(0xFF8E8E93)
    val SegmentSelectedText = Color(0xFFFFFFFF)

    // Typography
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF8E8E93)
    val TextMuted = Color(0xFF636366)
    val TextDark = Color(0xFF000000)

    // Icons
    val IconWhite = Color(0xFFFFFFFF)
    val IconMuted = Color(0xFF8E8E93)
    val IconDark = Color(0xFF000000)

    // Bottom Navigation Bar
    val BottomBarBackground = Color(0xFF000000)
    val BottomBarBorder = Color(0xFF1C1C1E)
    val NavPillActive = Color(0xFFFFFFFF)
    val NavTextActive = Color(0xFF000000)
    val NavInactive = Color(0xFF8E8E93)

    // Image Checkerboard Pattern for transparent backdrop
    val CheckerDark = Color(0xFF101010)
    val CheckerLight = Color(0xFF181818)

    // Grayscale Progress Bar Track & Gradient
    val ProgressTrack = Color(0xFF262626)
    val ProgressGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF4A4A4A),
            Color(0xFF8E8E93),
            Color(0xFFD1D1D6),
            Color(0xFFFFFFFF)
        )
    )

    // Subtle dark scrim for image sample labels
    val SampleOverlayScrim = Color(0xCC000000)
}

// Top-level aliases mapping to AppColors for compatibility
val PrimaryIndigo = AppColors.ActivePillBackground
val PrimaryIndigoDark = AppColors.ActivePillBackground
val OnPrimaryWhite = AppColors.ActivePillText
val PrimaryContainerIndigo = AppColors.DarkPillBackground
val PrimaryContainerIndigoDark = AppColors.DarkPillBackground
val OnPrimaryContainerIndigo = AppColors.TextPrimary
val OnPrimaryContainerIndigoDark = AppColors.TextPrimary

val SecondaryCyan = AppColors.ActivePillBackground
val SecondaryCyanDark = AppColors.ActivePillBackground
val SecondaryContainerCyan = AppColors.DarkPillBackground
val SecondaryContainerCyanDark = AppColors.DarkPillBackground
val OnSecondaryContainerCyan = AppColors.TextPrimary
val OnSecondaryContainerCyanDark = AppColors.TextPrimary

val TertiaryEmerald = AppColors.ActivePillBackground
val TertiaryEmeraldDark = AppColors.ActivePillBackground
val TertiaryContainerEmerald = AppColors.DarkPillBackground
val TertiaryContainerEmeraldDark = AppColors.DarkPillBackground

val SurfaceLight = AppColors.Background
val SurfaceCardLight = AppColors.CardBackground
val SurfaceContainerHighLight = AppColors.CardElevated
val OutlineLight = AppColors.CardBorder
val TextPrimaryLight = AppColors.TextPrimary
val TextSecondaryLight = AppColors.TextSecondary

val SurfaceDark = AppColors.Background
val SurfaceCardDark = AppColors.CardBackground
val SurfaceContainerHighDark = AppColors.CardElevated
val OutlineDark = AppColors.CardBorder
val TextPrimaryDark = AppColors.TextPrimary
val TextSecondaryDark = AppColors.TextSecondary

val ElectricGradient = AppColors.ProgressGradient
val EmeraldGradient = AppColors.ProgressGradient
val AmberGradient = AppColors.ProgressGradient
val RoseGradient = AppColors.ProgressGradient
val PurpleGradient = AppColors.ProgressGradient
