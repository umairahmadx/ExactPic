package com.umair.exactpic.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.ImageBitmap
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umair.exactpic.ui.theme.AppColors

/**
 * Shape that clips content horizontally from the left edge up to [splitFraction] (0f to 1f).
 */
class SplitClipShape(private val splitFraction: Float) : Shape {
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

/**
 * A split-screen image comparison slider composable using horizontal drag gestures
 * to interactively compare "before" (original) and "after" (modified) images.
 *
 * @param beforeImage The baseline / before image.
 * @param afterImage The modified / after image.
 * @param modifier Composable modifier.
 * @param initialSplitFraction Initial divider position between 0f (all after) and 1f (all before). Default is 0.5f.
 * @param beforeLabel Optional label shown on the before (left) side.
 * @param afterLabel Optional label shown on the after (right) side.
 * @param dividerColor Color of the vertical divider line.
 * @param dividerWidth Thickness of the vertical divider line.
 * @param handleSize Diameter of the circular drag handle thumb.
 * @param showLabels Whether to display before/after badges.
 * @param contentScale Scaling behavior for both images.
 * @param onSplitFractionChanged Callback invoked when the user drags the slider.
 */
@Composable
fun SplitScreenImageSlider(
    beforeImage: ImageBitmap,
    afterImage: ImageBitmap,
    modifier: Modifier = Modifier,
    initialSplitFraction: Float = 0.5f,
    beforeLabel: String = "BEFORE",
    afterLabel: String = "AFTER",
    dividerColor: Color = Color.White,
    dividerWidth: Dp = 2.5.dp,
    handleSize: Dp = 38.dp,
    showLabels: Boolean = true,
    contentScale: ContentScale = ContentScale.Fit,
    onSplitFractionChanged: ((Float) -> Unit)? = null
) {
    SplitScreenContentSlider(
        modifier = modifier,
        initialSplitFraction = initialSplitFraction,
        beforeLabel = beforeLabel,
        afterLabel = afterLabel,
        dividerColor = dividerColor,
        dividerWidth = dividerWidth,
        handleSize = handleSize,
        showLabels = showLabels,
        onSplitFractionChanged = onSplitFractionChanged,
        beforeContent = {
            Image(
                bitmap = beforeImage,
                contentDescription = beforeLabel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                contentScale = contentScale
            )
        },
        afterContent = {
            Image(
                bitmap = afterImage,
                contentDescription = afterLabel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                contentScale = contentScale
            )
        }
    )
}

/**
 * Overload of [SplitScreenImageSlider] accepting standard Android [Bitmap] instances.
 */
@Composable
fun SplitScreenImageSlider(
    beforeBitmap: Bitmap,
    afterBitmap: Bitmap,
    modifier: Modifier = Modifier,
    initialSplitFraction: Float = 0.5f,
    beforeLabel: String = "BEFORE",
    afterLabel: String = "AFTER",
    dividerColor: Color = Color.White,
    dividerWidth: Dp = 2.5.dp,
    handleSize: Dp = 38.dp,
    showLabels: Boolean = true,
    contentScale: ContentScale = ContentScale.Fit,
    onSplitFractionChanged: ((Float) -> Unit)? = null
) {
    val beforeImageBitmap = remember(beforeBitmap) { beforeBitmap.asImageBitmap() }
    val afterImageBitmap = remember(afterBitmap) { afterBitmap.asImageBitmap() }

    SplitScreenImageSlider(
        beforeImage = beforeImageBitmap,
        afterImage = afterImageBitmap,
        modifier = modifier,
        initialSplitFraction = initialSplitFraction,
        beforeLabel = beforeLabel,
        afterLabel = afterLabel,
        dividerColor = dividerColor,
        dividerWidth = dividerWidth,
        handleSize = handleSize,
        showLabels = showLabels,
        contentScale = contentScale,
        onSplitFractionChanged = onSplitFractionChanged
    )
}

/**
 * Generic split-screen composable that handles horizontal drag gestures across two arbitrary content slots.
 */
@Composable
fun SplitScreenContentSlider(
    beforeContent: @Composable () -> Unit,
    afterContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialSplitFraction: Float = 0.5f,
    beforeLabel: String = "BEFORE",
    afterLabel: String = "AFTER",
    dividerColor: Color = Color.White,
    dividerWidth: Dp = 2.5.dp,
    handleSize: Dp = 38.dp,
    showLabels: Boolean = true,
    onSplitFractionChanged: ((Float) -> Unit)? = null
) {
    var splitFraction by remember {
        mutableFloatStateOf(initialSplitFraction.coerceIn(0f, 1f))
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, AppColors.CardBorder, RoundedCornerShape(16.dp))
            // Horizontal drag gesture detector
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    val newFraction = (splitFraction + (dragAmount / size.width)).coerceIn(0f, 1f)
                    splitFraction = newFraction
                    onSplitFractionChanged?.invoke(newFraction)
                }
            }
            // Tap gesture detector to jump slider position
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newFraction = (offset.x / size.width).coerceIn(0f, 1f)
                    splitFraction = newFraction
                    onSplitFractionChanged?.invoke(newFraction)
                }
            }
            .testTag("split_screen_comparison_slider")
    ) {
        val totalWidth = maxWidth

        // 1. Bottom Layer: "After" content (revealed as slider moves left)
        CheckerboardBox(modifier = Modifier.fillMaxSize()) {
            afterContent()
        }

        // 2. Top Layer: "Before" content (clipped from 0f to splitFraction)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(SplitClipShape(splitFraction))
        ) {
            CheckerboardBox(modifier = Modifier.fillMaxSize()) {
                beforeContent()
            }
        }

        // 3. Vertical Divider Line
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(dividerWidth)
                .align(Alignment.CenterStart)
                .offset(x = (totalWidth * splitFraction) - (dividerWidth / 2))
                .background(dividerColor)
        )

        // 4. Draggable Center Handle with Chevron Arrows
        Box(
            modifier = Modifier
                .size(handleSize)
                .align(Alignment.CenterStart)
                .offset(x = (totalWidth * splitFraction) - (handleSize / 2))
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, AppColors.DarkPillBorder, CircleShape)
                .testTag("split_slider_handle"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(handleSize * 0.45f)) {
                val leftChevron = Path().apply {
                    moveTo(size.width * 0.32f, size.height * 0.2f)
                    lineTo(size.width * 0.05f, size.height * 0.5f)
                    lineTo(size.width * 0.32f, size.height * 0.8f)
                    close()
                }
                val rightChevron = Path().apply {
                    moveTo(size.width * 0.68f, size.height * 0.2f)
                    lineTo(size.width * 0.95f, size.height * 0.5f)
                    lineTo(size.width * 0.68f, size.height * 0.8f)
                    close()
                }
                drawPath(leftChevron, color = Color(0xFF111418))
                drawPath(rightChevron, color = Color(0xFF111418))
                drawLine(
                    color = Color(0xFF888E99),
                    start = Offset(size.width * 0.5f, size.height * 0.15f),
                    end = Offset(size.width * 0.5f, size.height * 0.85f),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }

        // 5. Floating Labels
        if (showLabels) {
            if (beforeLabel.isNotEmpty()) {
                SliderLabelBadge(
                    text = beforeLabel,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    isHighlighted = false
                )
            }

            if (afterLabel.isNotEmpty()) {
                SliderLabelBadge(
                    text = afterLabel,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    isHighlighted = true
                )
            }
        }
    }
}

@Composable
private fun SliderLabelBadge(
    text: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xDD111418))
            .border(
                1.dp,
                if (isHighlighted) AppColors.ActivePillBackground.copy(alpha = 0.6f) else Color(0x33FFFFFF),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = if (isHighlighted) AppColors.ActivePillBackground else Color.White,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
