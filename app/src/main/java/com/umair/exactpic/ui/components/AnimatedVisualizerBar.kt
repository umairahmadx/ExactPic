package com.umair.exactpic.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umair.exactpic.ui.theme.AppColors
import java.util.Locale

@Composable
fun AnimatedVisualizerBar(
    currentKb: Double,
    targetKb: Double,
    modifier: Modifier = Modifier
) {
    if (currentKb <= 0 || targetKb <= 0) return

    val isPadded = targetKb > currentKb
    val isCompressed = targetKb < currentKb
    val isSame = kotlin.math.abs(targetKb - currentKb) < 0.5

    // Animate the fill percentage smoothly
    val targetFillFraction = when {
        isSame -> 0.5f
        isCompressed -> ((targetKb / currentKb) * 0.5).toFloat().coerceIn(0.08f, 0.48f)
        else -> {
            val excessRatio = ((targetKb - currentKb) / currentKb).toFloat()
            (0.5f + (excessRatio * 0.25f)).coerceIn(0.52f, 1f)
        }
    }

    val animatedFraction by animateFloatAsState(
        targetValue = targetFillFraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "targetFillAnim"
    )

    // Shimmer effect for padding bytes
    val infiniteTransition = rememberInfiniteTransition(label = "stripeAnim")
    val stripeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stripeOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.CardBackground)
            .border(1.dp, AppColors.CardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(AppColors.DarkPillBackground)
                            .border(1.dp, AppColors.DarkPillBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isPadded -> Icons.Default.ArrowUpward
                                isCompressed -> Icons.Default.ArrowDownward
                                else -> Icons.Default.Balance
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = AppColors.IconWhite
                        )
                    }

                    Text(
                        text = when {
                            isPadded -> "Padded Size Growth"
                            isCompressed -> "File Size Compression"
                            else -> "Original File Size Match"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.DarkPillBackground)
                        .border(1.dp, AppColors.DarkPillBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    val diffKb = targetKb - currentKb
                    val percent = ((targetKb - currentKb) / currentKb) * 100.0
                    Text(
                        text = when {
                            isPadded -> "+%.1f KB (+%.0f%%)".format(Locale.US, diffKb, percent)
                            isCompressed -> "%.1f KB (%.0f%%)".format(Locale.US, diffKb, percent)
                            else -> "Exact Match (100%)"
                        },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                }
            }

            // Animated Visual Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(AppColors.ProgressTrack)
            ) {
                // Baseline marker at 50%
                Canvas(modifier = Modifier.fillMaxWidth().height(14.dp)) {
                    val midX = size.width * 0.5f
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = Offset(midX, 0f),
                        end = Offset(midX, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Dynamic filled segment with grayscale gradient
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedFraction)
                        .clip(RoundedCornerShape(7.dp))
                        .background(AppColors.ProgressGradient)
                )

                // Shimmering pattern if padded
                if (isPadded) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedFraction)
                    ) {
                        val barWidth = size.width
                        val barHeight = size.height
                        var x = -60f + (stripeOffset % 30f)
                        while (x < barWidth) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.25f),
                                start = Offset(x, 0f),
                                end = Offset(x + 15f, barHeight),
                                strokeWidth = 3.dp.toPx()
                            )
                            x += 24f
                        }
                    }
                }
            }

            // Indicators below bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Original: %.1f KB".format(Locale.US, currentKb),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AppColors.TextSecondary
                )
                Text(
                    text = "Target: %.1f KB".format(Locale.US, targetKb),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            }
        }
    }
}
