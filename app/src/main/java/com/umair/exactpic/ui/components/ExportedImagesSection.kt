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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
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

@Composable
fun ExportedImagesSection(
    exportedImages: List<ExportedImageItem>,
    onDownload: (ExportedImageItem) -> Unit,
    onShare: (ExportedImageItem) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(exportedImages, key = { it.id }) { item ->
                ExportedImageCard(
                    item = item,
                    onDownload = { onDownload(item) },
                    onShare = { onShare(item) }
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
    modifier: Modifier = Modifier
) {
    val bitmap = remember(item.bytes) {
        BitmapFactory.decodeByteArray(item.bytes, 0, item.bytes.size)?.asImageBitmap()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exported_card_${item.fileName}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(AppColors.CardBorder))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Image Container with Checkerboard Backdrop
            CheckerboardBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
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

            // Bottom Filename and Action Icons Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.fileName,
                    color = AppColors.TextPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("download_${item.fileName}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Download image",
                            tint = AppColors.IconWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("share_${item.fileName}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share image",
                            tint = AppColors.IconWhite,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}
