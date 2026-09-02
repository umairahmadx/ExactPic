package com.umair.exactpic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.umair.exactpic.util.ImageUtils

@Composable
fun EmptyCanvasCard(
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("empty_canvas_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AppColors.CardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upload Circle Icon
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(AppColors.CircleIconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudUpload,
                    contentDescription = "Upload Cloud Icon",
                    tint = AppColors.IconWhite,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Select Image",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Upload an image to start padding.",
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = AppColors.Divider, thickness = 1.dp)

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Gallery and Camera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gallery button (Dark)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.DarkPillBackground)
                        .border(1.dp, AppColors.DarkPillBorder, RoundedCornerShape(14.dp))
                        .clickable(onClick = onPickGallery)
                        .testTag("gallery_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = AppColors.IconWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Gallery",
                            color = AppColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                }

                // Camera button (White)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.ActivePillBackground)
                        .clickable(onClick = onTakePhoto)
                        .testTag("camera_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = null,
                            tint = AppColors.IconDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Camera",
                            color = AppColors.TextDark,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StandardSamplesRow(
    onSelectSample: (ImageFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate bitmap thumbnails for standard samples
    val jpegThumbnail = remember {
        ImageUtils.createArtCubesBitmap(160, 160).asImageBitmap()
    }
    val pngThumbnail = remember {
        ImageUtils.createMotherboardBitmap(160, 160).asImageBitmap()
    }
    val webpThumbnail = remember {
        ImageUtils.createCosmicSpheresBitmap(160, 160).asImageBitmap()
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "TRY WITH STANDARD SAMPLES",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = AppColors.TextSecondary,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SampleThumbnailCard(
                label = "JPEG",
                bitmap = jpegThumbnail,
                onClick = { onSelectSample(ImageFormat.JPEG) },
                modifier = Modifier.weight(1f)
            )
            SampleThumbnailCard(
                label = "PNG",
                bitmap = pngThumbnail,
                onClick = { onSelectSample(ImageFormat.PNG) },
                modifier = Modifier.weight(1f)
            )
            SampleThumbnailCard(
                label = "WebP",
                bitmap = webpThumbnail,
                onClick = { onSelectSample(ImageFormat.WEBP) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SampleThumbnailCard(
    label: String,
    bitmap: androidx.compose.ui.graphics.ImageBitmap?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.CardBackground)
            .border(1.dp, AppColors.CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("sample_${label.lowercase()}_card"),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "$label sample",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Bottom label bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.SampleOverlayScrim)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = AppColors.TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
