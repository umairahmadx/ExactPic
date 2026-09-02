package com.umair.exactpic.ui.components

import android.graphics.BitmapFactory
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umair.exactpic.model.ImageFormat
import com.umair.exactpic.ui.theme.AppColors
import com.umair.exactpic.viewmodel.PadderUiState

@Composable
fun SourceImageSection(
    uiState: PadderUiState,
    onChangeImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val meta = uiState.originalMetadata ?: return
    val bytes = uiState.originalBytes ?: return

    val bitmap = remember(bytes) {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("source_image_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AppColors.CardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Title and Change button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source Image",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                // "Change" pill button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.DarkPillBackground)
                        .border(1.dp, AppColors.DarkPillBorder, RoundedCornerShape(10.dp))
                        .clickable(onClick = onChangeImage)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("change_image_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Change Image",
                            tint = AppColors.IconWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Change",
                            color = AppColors.TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Checkerboard Image Preview Box
            CheckerboardBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, AppColors.CardBorder, RoundedCornerShape(14.dp))
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Source Image Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3-Column Metadata Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Column 1: Current Size
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Size",
                        fontSize = 11.sp,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = meta.formattedKB,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary
                    )
                }

                // Column 2: Dimensions
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Dimensions",
                        fontSize = 11.sp,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${meta.width}×${meta.height}",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary
                    )
                }

                // Column 3: Format
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Format",
                        fontSize = 11.sp,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = when (meta.format) {
                            ImageFormat.JPEG -> "JPEG"
                            ImageFormat.PNG -> "PNG"
                            ImageFormat.WEBP -> "WebP"
                            else -> meta.format.name
                        },
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary
                    )
                }
            }
        }
    }
}
