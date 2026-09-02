package com.umair.exactpic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umair.exactpic.ui.theme.AppColors
import com.umair.exactpic.viewmodel.PadderUiState

@Composable
fun TargetSizeCard(
    uiState: PadderUiState,
    onTargetSizeChange: (String) -> Unit,
    onQuickAdjustment: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val meta = uiState.originalMetadata ?: return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("target_size_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(AppColors.CardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header with Tune icon and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = AppColors.IconWhite,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Target File Size",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Desired Size (KB)
            Text(
                text = "DESIRED SIZE (KB)",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = AppColors.TextSecondary,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Custom styled Input Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.InputBackground)
                    .border(1.dp, AppColors.InputBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BasicTextField(
                        value = uiState.targetSizeKb,
                        onValueChange = onTargetSizeChange,
                        textStyle = TextStyle(
                            color = AppColors.InputText,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        cursorBrush = SolidColor(AppColors.ActivePillBackground),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("target_size_input")
                    )

                    Text(
                        text = "KB",
                        color = AppColors.InputTrailingText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scale Factor
            Text(
                text = "SCALE FACTOR",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = AppColors.TextSecondary,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.InputBackground)
                    .border(1.dp, AppColors.InputBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = uiState.scaleFactorText,
                        color = AppColors.TextPrimary,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )

                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Aspect Locked",
                        tint = AppColors.IconMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Adjustments
            Text(
                text = "QUICK ADJUSTMENTS",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = AppColors.TextSecondary,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Row 1: -10%, -5%, Original, +5%
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAdjustmentPill(
                    label = "-10%",
                    isSelected = uiState.selectedQuickAdjustment == -10,
                    onClick = { onQuickAdjustment(-10) },
                    modifier = Modifier.weight(1f)
                )
                QuickAdjustmentPill(
                    label = "-5%",
                    isSelected = uiState.selectedQuickAdjustment == -5,
                    onClick = { onQuickAdjustment(-5) },
                    modifier = Modifier.weight(1f)
                )
                QuickAdjustmentPill(
                    label = "Original",
                    isSelected = uiState.selectedQuickAdjustment == 0,
                    onClick = { onQuickAdjustment(0) },
                    modifier = Modifier.weight(1.2f)
                )
                QuickAdjustmentPill(
                    label = "+5%",
                    isSelected = uiState.selectedQuickAdjustment == 5,
                    onClick = { onQuickAdjustment(5) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: +10%, +25%
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAdjustmentPill(
                    label = "+10%",
                    isSelected = uiState.selectedQuickAdjustment == 10,
                    onClick = { onQuickAdjustment(10) },
                    modifier = Modifier.weight(1f)
                )
                QuickAdjustmentPill(
                    label = "+25%",
                    isSelected = uiState.selectedQuickAdjustment == 25,
                    onClick = { onQuickAdjustment(25) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(2.2f))
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Padded Size Growth Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Padded Size Growth",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AppColors.TextSecondary
                )
                Text(
                    text = uiState.growthPercentageText,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grayscale Gradient Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.ProgressTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(uiState.progressFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppColors.ProgressGradient)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Indicators below progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = meta.formattedKB,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AppColors.TextSecondary
                )
                Text(
                    text = "${uiState.targetSizeKb.ifEmpty { "0" }} KB",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AppColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun QuickAdjustmentPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) AppColors.ActivePillBackground else AppColors.DarkPillBackground
    val textColor = if (isSelected) AppColors.ActivePillText else AppColors.DarkPillText
    val borderCol = if (isSelected) AppColors.ActivePillBackground else AppColors.DarkPillBorder

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("quick_adj_${label.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
