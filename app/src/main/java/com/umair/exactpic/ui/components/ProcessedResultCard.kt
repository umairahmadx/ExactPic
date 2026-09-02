package com.umair.exactpic.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umair.exactpic.ui.theme.AppColors
import com.umair.exactpic.viewmodel.PadderUiState
import kotlinx.coroutines.delay

enum class CompareViewMode { RESULT, ORIGINAL, SPLIT }

@Composable
fun ProcessedResultCard(
    uiState: PadderUiState,
    onSaveToGallery: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val processed = uiState.processedMetadata ?: return
    val processedBytes = uiState.processedBytes ?: return
    val originalBytes = uiState.originalBytes

    var viewMode by remember { mutableStateOf(CompareViewMode.RESULT) }

    // Celebratory checkmark pop animation
    var popState by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        popState = true
    }
    val checkmarkScale by animateFloatAsState(
        targetValue = if (popState) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("processed_result_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(AppColors.CardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with animated checkmark and badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .scale(checkmarkScale)
                            .clip(CircleShape)
                            .background(AppColors.ActivePillBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = AppColors.IconDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Processing Complete!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = "New specification verified",
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.DarkPillBackground)
                        .border(1.dp, AppColors.DarkPillBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${uiState.sizeDiffKb} (${uiState.sizeDiffPercent})",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                }
            }

            // Interactive Compare Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View:",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AppColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                CompareModePill(
                    label = "Result",
                    isSelected = viewMode == CompareViewMode.RESULT,
                    onClick = { viewMode = CompareViewMode.RESULT }
                )
                CompareModePill(
                    label = "Original",
                    isSelected = viewMode == CompareViewMode.ORIGINAL,
                    onClick = { viewMode = CompareViewMode.ORIGINAL }
                )
                CompareModePill(
                    label = "Split",
                    isSelected = viewMode == CompareViewMode.SPLIT,
                    onClick = { viewMode = CompareViewMode.SPLIT }
                )
            }

            // Preview Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, AppColors.CardBorder, RoundedCornerShape(14.dp))
            ) {
                Crossfade(targetState = viewMode, label = "compareViewCrossfade") { mode ->
                    when (mode) {
                        CompareViewMode.RESULT -> {
                            val bitmap = remember(processedBytes) {
                                BitmapFactory.decodeByteArray(processedBytes, 0, processedBytes.size)
                            }
                            CheckerboardBox(modifier = Modifier.fillMaxSize()) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Processed image preview",
                                        modifier = Modifier.fillMaxSize().padding(6.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                        CompareViewMode.ORIGINAL -> {
                            val bitmap = remember(originalBytes) {
                                originalBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                            }
                            CheckerboardBox(modifier = Modifier.fillMaxSize()) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Original image preview",
                                        modifier = Modifier.fillMaxSize().padding(6.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                        CompareViewMode.SPLIT -> {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val origBmp = remember(originalBytes) {
                                    originalBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                                }
                                CheckerboardBox(modifier = Modifier.weight(1f).fillMaxSize()) {
                                    if (origBmp != null) {
                                        Image(
                                            bitmap = origBmp.asImageBitmap(),
                                            contentDescription = "Original",
                                            modifier = Modifier.fillMaxSize().padding(4.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }

                                val procBmp = remember(processedBytes) {
                                    BitmapFactory.decodeByteArray(processedBytes, 0, processedBytes.size)
                                }
                                CheckerboardBox(modifier = Modifier.weight(1f).fillMaxSize()) {
                                    if (procBmp != null) {
                                        Image(
                                            bitmap = procBmp.asImageBitmap(),
                                            contentDescription = "Processed",
                                            modifier = Modifier.fillMaxSize().padding(4.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Properties row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResultPropertyChip(
                    label = "New Size",
                    value = processed.formattedKB,
                    modifier = Modifier.weight(1f)
                )
                ResultPropertyChip(
                    label = "Dimensions",
                    value = "${processed.width}×${processed.height}",
                    modifier = Modifier.weight(1f)
                )
                ResultPropertyChip(
                    label = "Format",
                    value = processed.format.displayName,
                    modifier = Modifier.weight(0.9f)
                )
            }

            // Save and Share Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Save Button (White)
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.ActivePillBackground)
                        .clickable(onClick = onSaveToGallery)
                        .testTag("save_gallery_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = AppColors.IconDark, modifier = Modifier.size(18.dp))
                        Text("Save to Gallery", color = AppColors.TextDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Share Button (Dark)
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.DarkPillBackground)
                        .border(1.dp, AppColors.DarkPillBorder, RoundedCornerShape(12.dp))
                        .clickable(onClick = onShare)
                        .testTag("share_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = AppColors.IconWhite, modifier = Modifier.size(18.dp))
                        Text("Share", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareModePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) AppColors.ActivePillBackground else AppColors.DarkPillBackground
    val textColor = if (isSelected) AppColors.ActivePillText else AppColors.DarkPillText
    val borderCol = if (isSelected) AppColors.ActivePillBackground else AppColors.DarkPillBorder

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ResultPropertyChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.InputBackground)
            .border(1.dp, AppColors.InputBorder, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
        }
    }
}
