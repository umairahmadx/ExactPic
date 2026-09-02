package com.umair.exactpic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umair.exactpic.model.DimensionUnit
import com.umair.exactpic.ui.theme.AppColors
import com.umair.exactpic.viewmodel.PadderUiState

@Composable
fun ResizeOptionCard(
    uiState: PadderUiState,
    onToggleResize: () -> Unit,
    onUnitChange: (DimensionUnit) -> Unit,
    onWidthChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onToggleLock: () -> Unit,
    onScalePreset: (Int) -> Unit,
    onDimensionsPreset: (Double, Double, DimensionUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("resize_dimensions_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(AppColors.CardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Title and Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resize & Dimensions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                Switch(
                    checked = uiState.isResizeEnabled,
                    onCheckedChange = { onToggleResize() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.ActivePillBackground,
                        checkedTrackColor = AppColors.DarkPillBorder,
                        uncheckedThumbColor = AppColors.TextSecondary,
                        uncheckedTrackColor = AppColors.InputBackground,
                        uncheckedBorderColor = AppColors.InputBorder
                    ),
                    modifier = Modifier.testTag("resize_switch")
                )
            }

            // Expandable configuration when resize is enabled
            AnimatedVisibility(
                visible = uiState.isResizeEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Unit Selector Segmented Pills: Pixels, Inches, Cm
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.SegmentContainer)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        UnitSegmentTab(
                            title = "Pixels",
                            isSelected = uiState.dimensionUnit == DimensionUnit.PIXELS,
                            onClick = { onUnitChange(DimensionUnit.PIXELS) },
                            modifier = Modifier.weight(1f)
                        )
                        UnitSegmentTab(
                            title = "Inches",
                            isSelected = uiState.dimensionUnit == DimensionUnit.INCHES,
                            onClick = { onUnitChange(DimensionUnit.INCHES) },
                            modifier = Modifier.weight(1f)
                        )
                        UnitSegmentTab(
                            title = "Cm",
                            isSelected = uiState.dimensionUnit == DimensionUnit.CENTIMETERS,
                            onClick = { onUnitChange(DimensionUnit.CENTIMETERS) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // WIDTH and HEIGHT Inputs with Chain Link in Middle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Width Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WIDTH",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AppColors.TextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            DimensionInputBox(
                                value = uiState.overrideWidth,
                                onValueChange = onWidthChange,
                                unitSymbol = uiState.dimensionUnit.symbol,
                                testTag = "width_input"
                            )
                        }

                        // Middle aspect-ratio lock button
                        Box(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppColors.SegmentSelected)
                                .border(1.dp, AppColors.InputBorder, CircleShape)
                                .clickable(onClick = onToggleLock)
                                .testTag("aspect_ratio_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isAspectRatioLocked) Icons.Default.Link else Icons.Default.LinkOff,
                                contentDescription = "Aspect Ratio Lock",
                                tint = AppColors.IconWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Height Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "HEIGHT",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AppColors.TextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            DimensionInputBox(
                                value = uiState.overrideHeight,
                                onValueChange = onHeightChange,
                                unitSymbol = uiState.dimensionUnit.symbol,
                                testTag = "height_input"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Scale Percentage
                    Text(
                        text = "SCALE PERCENTAGE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AppColors.TextSecondary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2x2 Grid of Scale Percentage
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScalePercentageButton(
                                label = "100%",
                                isSelected = uiState.scalePresetPercentage == 100,
                                onClick = { onScalePreset(100) },
                                modifier = Modifier.weight(1f)
                            )
                            ScalePercentageButton(
                                label = "75%",
                                isSelected = uiState.scalePresetPercentage == 75,
                                onClick = { onScalePreset(75) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScalePercentageButton(
                                label = "50%",
                                isSelected = uiState.scalePresetPercentage == 50,
                                onClick = { onScalePreset(50) },
                                modifier = Modifier.weight(1f)
                            )
                            ScalePercentageButton(
                                label = "25%",
                                isSelected = uiState.scalePresetPercentage == 25,
                                onClick = { onScalePreset(25) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Standard Sizes
                    Text(
                        text = "STANDARD SIZES",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AppColors.TextSecondary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StandardSizePill(
                            label = "FHD",
                            onClick = { onDimensionsPreset(1920.0, 1080.0, DimensionUnit.PIXELS) },
                            modifier = Modifier.weight(1f)
                        )
                        StandardSizePill(
                            label = "HD",
                            onClick = { onDimensionsPreset(1280.0, 720.0, DimensionUnit.PIXELS) },
                            modifier = Modifier.weight(1f)
                        )
                        StandardSizePill(
                            label = "4K",
                            onClick = { onDimensionsPreset(3840.0, 2160.0, DimensionUnit.PIXELS) },
                            modifier = Modifier.weight(1f)
                        )
                        StandardSizePill(
                            label = "Social Sq",
                            onClick = { onDimensionsPreset(1080.0, 1080.0, DimensionUnit.PIXELS) },
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitSegmentTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) AppColors.SegmentSelected else Color.Transparent
    val textCol = if (isSelected) AppColors.SegmentSelectedText else AppColors.SegmentUnselectedText

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textCol,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun DimensionInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    unitSymbol: String,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.InputBackground)
            .border(1.dp, AppColors.InputBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
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
                    .testTag(testTag)
            )

            Text(
                text = unitSymbol,
                color = AppColors.InputTrailingText,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ScalePercentageButton(
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
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("scale_btn_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun StandardSizePill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.DarkPillBackground)
            .border(1.dp, AppColors.DarkPillBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("std_size_${label.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = AppColors.TextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
