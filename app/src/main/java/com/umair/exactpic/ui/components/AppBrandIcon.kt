package com.umair.exactpic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.umair.exactpic.ui.theme.AppColors

import androidx.compose.material3.Icon
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.umair.exactpic.R

/**
 * ExactPic App Logo matching the user's precision monogram and dimension vector:
 * Reduced to a perfect, compact, sharp size for modern top app bars.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    iconSize: Dp = 18.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(7.dp))
            .background(AppColors.CardElevated)
            .border(1.dp, AppColors.CardBorder, RoundedCornerShape(7.dp))
            .testTag("app_logo"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            tint = AppColors.IconWhite,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Alias for AppLogo for concise referencing.
 */
@Composable
fun Logo(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    iconSize: Dp = 18.dp
) {
    AppLogo(modifier = modifier, size = size, iconSize = iconSize)
}

/**
 * Backward-compatible wrapper delegating to the new compact AppLogo.
 */
@Composable
fun AppBrandIcon(modifier: Modifier = Modifier) {
    AppLogo(modifier = modifier, size = 28.dp, iconSize = 18.dp)
}


/**
 * Checkerboard pattern backdrop for image transparency preview.
 */
@Composable
fun CheckerboardBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .drawBehind {
                val squareSize = 14.dp.toPx()
                val numX = (size.width / squareSize).toInt() + 1
                val numY = (size.height / squareSize).toInt() + 1
                for (i in 0 until numX) {
                    for (j in 0 until numY) {
                        val isEven = (i + j) % 2 == 0
                        drawRect(
                            color = if (isEven) AppColors.CheckerLight else AppColors.CheckerDark,
                            topLeft = Offset(i * squareSize, j * squareSize),
                            size = Size(squareSize, squareSize)
                        )
                    }
                }
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}
