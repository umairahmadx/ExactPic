package com.umair.exactpic.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umair.exactpic.model.ImageMetadata
import com.umair.exactpic.ui.theme.AppColors
import kotlin.math.roundToInt

/**
 * Custom Shape that clips content horizontally from the left edge to [splitFraction] (0f to 1f).
 */
class HorizontalSplitShape(private val splitFraction: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val clamped = splitFraction.coerceIn(0f, 1f)
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = 0f,
                right = size.width * clamped,
                bottom = size.height
            )
        )
    }
}

enum class ComparisonViewMode(val label: String) {
    SLIDER("Split Slider"),
    SIDE_BY_SIDE("Side by Side"),
    FLIP("Flip View")
}

/**
 * Interactive split-screen image comparison slider component comparing Old vs New images.
 */
@Composable
fun ImageComparisonSlider(
    originalBytes: ByteArray?,
    originalMetadata: ImageMetadata?,
    processedBytes: ByteArray?,
    processedMetadata: ImageMetadata?,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(ComparisonViewMode.SLIDER) }
    var splitPosition by remember { mutableFloatStateOf(0.5f) }
    var isShowingNewInFlip by remember { mutableStateOf(true) }

    val origBitmap = remember(originalBytes) {
        originalBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }
    val procBitmap = remember(processedBytes) {
        processedBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mode Selection Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Compare View:",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ComparisonViewMode.values().forEach { mode ->
                    ModeTabPill(
                        label = mode.label,
                        isSelected = viewMode == mode,
                        onClick = { viewMode = mode }
                    )
                }
            }
        }

        // Main Viewer Frame
        Crossfade(targetState = viewMode, label = "comparisonModeCrossfade") { mode ->
            when (mode) {
                ComparisonViewMode.SLIDER -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // The Split Screen Wipe Box
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, AppColors.CardBorder, RoundedCornerShape(16.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val newPos = (splitPosition + dragAmount.x / size.width).coerceIn(0f, 1f)
                                        splitPosition = newPos
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        splitPosition = (offset.x / size.width).coerceIn(0f, 1f)
                                    }
                                }
                                .testTag("comparison_slider_viewer")
                        ) {
                            val containerWidth = maxWidth

                            // 1. Bottom Layer: Processed (New) Image
                            CheckerboardBox(modifier = Modifier.fillMaxSize()) {
                                if (procBitmap != null) {
                                    Image(
                                        bitmap = procBitmap.asImageBitmap(),
                                        contentDescription = "New processed image",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }

                            // 2. Top Layer: Original (Old) Image clipped to splitPosition
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(HorizontalSplitShape(splitPosition))
                            ) {
                                CheckerboardBox(modifier = Modifier.fillMaxSize()) {
                                    if (origBitmap != null) {
                                        Image(
                                            bitmap = origBitmap.asImageBitmap(),
                                            contentDescription = "Old original image",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }

                            // 3. Vertical Divider Line
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(2.5.dp)
                                    .align(Alignment.CenterStart)
                                    .offset(x = (containerWidth * splitPosition) - 1.25.dp)
                                    .background(Color.White)
                            )

                            // 4. Draggable Thumb Handle with Arrow Icons
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .align(Alignment.CenterStart)
                                    .offset(x = (containerWidth * splitPosition) - 19.dp)
                                    .shadow(8.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, AppColors.DarkPillBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    val leftArrow = Path().apply {
                                        moveTo(size.width * 0.32f, size.height * 0.2f)
                                        lineTo(size.width * 0.05f, size.height * 0.5f)
                                        lineTo(size.width * 0.32f, size.height * 0.8f)
                                        close()
                                    }
                                    val rightArrow = Path().apply {
                                        moveTo(size.width * 0.68f, size.height * 0.2f)
                                        lineTo(size.width * 0.95f, size.height * 0.5f)
                                        lineTo(size.width * 0.68f, size.height * 0.8f)
                                        close()
                                    }
                                    drawPath(leftArrow, color = Color(0xFF111418))
                                    drawPath(rightArrow, color = Color(0xFF111418))
                                    drawLine(
                                        color = Color(0xFF888E99),
                                        start = Offset(size.width * 0.5f, size.height * 0.15f),
                                        end = Offset(size.width * 0.5f, size.height * 0.85f),
                                        strokeWidth = 1.5.dp.toPx()
                                    )
                                }
                            }

                            // 5. Floating Labels (Old on Left, New on Right)
                            FloatingLabelBadge(
                                text = "OLD • ${originalMetadata?.formattedKB ?: ""}",
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                                isHighlight = false
                            )

                            FloatingLabelBadge(
                                text = "NEW • ${processedMetadata?.formattedKB ?: ""}",
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                isHighlight = true
                            )

                            // Helper overlay cue at bottom center
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xBB111418))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "‹ Slide to compare › ${(splitPosition * 100).roundToInt()}%",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFD1D5DB)
                                )
                            }
                        }

                        // Fine-tuning Controls & Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "100% Old",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AppColors.TextSecondary,
                                modifier = Modifier.clickable { splitPosition = 1f }
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PresetPill(
                                    label = "Old",
                                    isSelected = splitPosition == 1f,
                                    onClick = { splitPosition = 1f }
                                )
                                PresetPill(
                                    label = "50/50",
                                    isSelected = splitPosition in 0.48f..0.52f,
                                    onClick = { splitPosition = 0.5f }
                                )
                                PresetPill(
                                    label = "New",
                                    isSelected = splitPosition == 0f,
                                    onClick = { splitPosition = 0f }
                                )
                            }

                            Text(
                                text = "100% New",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AppColors.TextSecondary,
                                modifier = Modifier.clickable { splitPosition = 0f }
                            )
                        }

                        // Precision Slider
                        Slider(
                            value = splitPosition,
                            onValueChange = { splitPosition = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .testTag("comparison_ratio_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = AppColors.ActivePillBackground,
                                activeTrackColor = AppColors.ActivePillBackground,
                                inactiveTrackColor = AppColors.DarkPillBorder
                            )
                        )
                    }
                }

                ComparisonViewMode.SIDE_BY_SIDE -> {
                    // Dual Pane Layout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Original Pane
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, AppColors.CardBorder, RoundedCornerShape(14.dp))
                        ) {
                            CheckerboardBox(modifier = Modifier.fillMaxSize()) {
                                if (origBitmap != null) {
                                    Image(
                                        bitmap = origBitmap.asImageBitmap(),
                                        contentDescription = "Original",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                            FloatingLabelBadge(
                                text = "OLD • ${originalMetadata?.formattedKB ?: ""}",
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp),
                                isHighlight = false
                            )
                        }

                        // Processed Pane
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, AppColors.CardBorder, RoundedCornerShape(14.dp))
                        ) {
                            CheckerboardBox(modifier = Modifier.fillMaxSize()) {
                                if (procBitmap != null) {
                                    Image(
                                        bitmap = procBitmap.asImageBitmap(),
                                        contentDescription = "Processed",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                            FloatingLabelBadge(
                                text = "NEW • ${processedMetadata?.formattedKB ?: ""}",
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp),
                                isHighlight = true
                            )
                        }
                    }
                }

                ComparisonViewMode.FLIP -> {
                    // Interactive Tap to Flip Viewer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, AppColors.CardBorder, RoundedCornerShape(16.dp))
                            .clickable { isShowingNewInFlip = !isShowingNewInFlip }
                            .testTag("flip_view_card")
                    ) {
                        CheckerboardBox(modifier = Modifier.fillMaxSize()) {
                            val activeBitmap = if (isShowingNewInFlip) procBitmap else origBitmap
                            if (activeBitmap != null) {
                                Image(
                                    bitmap = activeBitmap.asImageBitmap(),
                                    contentDescription = if (isShowingNewInFlip) "New Image" else "Old Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        FloatingLabelBadge(
                            text = if (isShowingNewInFlip) "SHOWING: NEW (${processedMetadata?.formattedKB ?: ""})"
                            else "SHOWING: OLD (${originalMetadata?.formattedKB ?: ""})",
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp),
                            isHighlight = isShowingNewInFlip
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xCC111418))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "👆 Tap anywhere to flip image",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE5E7EB)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingLabelBadge(
    text: String,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xDD111418))
            .border(
                1.dp,
                if (isHighlight) AppColors.ActivePillBackground.copy(alpha = 0.6f) else Color(0x33FFFFFF),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = if (isHighlight) AppColors.ActivePillBackground else Color.White,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ModeTabPill(
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
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun PresetPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) AppColors.ActivePillBackground else AppColors.InputBackground
    val textCol = if (isSelected) AppColors.ActivePillText else AppColors.TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, if (isSelected) AppColors.ActivePillBackground else AppColors.InputBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textCol
        )
    }
}
