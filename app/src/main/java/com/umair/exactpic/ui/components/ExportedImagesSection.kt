package com.umair.exactpic.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umair.exactpic.ui.theme.AppColors
import com.umair.exactpic.viewmodel.ExportedImageItem
import com.umair.exactpic.model.ImageFormat

@Composable
fun ExportedImagesSection(
    exportedImages: List<ExportedImageItem>,
    onDownload: (ExportedImageItem) -> Unit,
    onShare: (ExportedImageItem) -> Unit,
    onDelete: (ExportedImageItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (exportedImages.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No exported images yet",
                    fontSize = 16.sp,
                    color = AppColors.TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pad and apply changes to see them here",
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exportedImages, key = { it.id }) { item ->
                ExportedImageCard(
                    item = item,
                    onDownload = { onDownload(item) },
                    onShare = { onShare(item) },
                    onDelete = { onDelete(item) }
                )
            }
        }
    }
}

@Composable
fun ExportedImageCard(
    item: ExportedImageItem,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(item.bytes) {
        BitmapFactory.decodeByteArray(item.bytes, 0, item.bytes.size)?.asImageBitmap()
    }
    val meta = item.metadata

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exported_card_${item.fileName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(AppColors.CardBorder)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image with aspect-ratio-aware sizing
            val aspectRatio = if (meta.height > 0) meta.width.toFloat() / meta.height.toFloat() else 1f
            val displayHeight = (320.dp * 1.0f / aspectRatio.max(0.5f).min(2.5f)).coerceIn(140.dp, 280.dp)

            CheckerboardBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(displayHeight)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = item.fileName,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Metadata & Actions Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top row: Format badge + Filename + Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Format badge
                    FormatBadge(format = meta.format)

                    // Filename (truncated if long)
                    Text(
                        text = item.fileName,
                        color = AppColors.TextPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_${item.fileName}")
                    ) {
                        Icon(
                            imageVector = Delete,
                            contentDescription = "Delete exported image",
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Metadata grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetadataChip(
                        icon = Crop,
                        label = "Dimensions",
                        value = "${meta.width} × ${meta.height} px"
                    )
                    MetadataChip(
                        icon = Storage,
                        label = "File Size",
                        value = meta.formattedKB
                    )
                    MetadataChip(
                        icon = AspectRatio,
                        label = "Aspect Ratio",
                        value = meta.aspectRatioLabel
                    )
                }

                // Divider
                androidx.compose.foundation.Divider(
                    color = AppColors.Divider,
                    thickness = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download button (primary)
                    ExportActionButton(
                        text = "Save",
                        icon = FileDownload,
                        onClick = onDownload,
                        isPrimary = true,
                        modifier = Modifier.weight(1f).testTag("download_${item.fileName}")
                    )

                    // Share button (secondary)
                    ExportActionButton(
                        text = "Share",
                        icon = Share,
                        onClick = onShare,
                        isPrimary = false,
                        modifier = Modifier.weight(1f).testTag("share_${item.fileName}")
                    )

                    // Info button
                    ExportActionButton(
                        text = "Details",
                        icon = Info,
                        onClick = { /* TODO: show detail dialog */ },
                        isPrimary = false,
                        modifier = Modifier.weight(1f).testTag("info_${item.fileName}")
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatBadge(format: ImageFormat) {
    val (bgColor, textColor, label) = when (format) {
        ImageFormat.JPEG -> AppColors.CardBorder to AppColors.TextSecondary to "JPEG"
        ImageFormat.PNG -> AppColors.CardBorder to AppColors.TextSecondary to "PNG"
        ImageFormat.WEBP -> AppColors.CardBorder to AppColors.TextSecondary to "WEBP"
        else -> AppColors.CardBorder to AppColors.TextSecondary to "RAW"
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MetadataChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(vertical = 4.dp)
            .background(AppColors.SurfaceDark, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = AppColors.TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = AppColors.TextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ExportActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isPrimary) AppColors.ActivePillBackground else AppColors.DarkPillBackground
    val textColor = if (isPrimary) AppColors.TextDark else AppColors.TextPrimary
    val borderColor = if (isPrimary) Color.Transparent else AppColors.DarkPillBorder

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor, RoundedCornerShape(10.dp))
            .border(if (isPrimary) 0.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                color = textColor,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
    }
}