package com.umair.exactpic.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umair.exactpic.ui.theme.AppColors

@Composable
fun PadderBottomBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.BottomBarBackground)
            .border(
                width = 1.dp,
                color = AppColors.BottomBarBorder,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
            ),
        containerColor = AppColors.BottomBarBackground,
        contentColor = Color.Unspecified
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Crop,
                    contentDescription = "Canvas",
                    tint = if (currentTab == 0) AppColors.NavTextActive else AppColors.NavInactive,
                    modifier = Modifier.testTag("nav_icon_canvas")
                )
            },
            label = {
                Text(
                    text = "Canvas",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal,
                    color = if (currentTab == 0) AppColors.NavTextActive else AppColors.NavInactive
                )
            },
            selected = currentTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.testTag("nav_tab_canvas"),
            selectedIconColor = AppColors.NavTextActive,
            unselectedIconColor = AppColors.NavInactive,
            selectedContentColor = AppColors.NavTextActive,
            unselectedContentColor = AppColors.NavInactive
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.FileUpload,
                    contentDescription = "Export",
                    tint = if (currentTab == 1) AppColors.NavTextActive else AppColors.NavInactive,
                    modifier = Modifier.testTag("nav_icon_export")
                )
            },
            label = {
                Text(
                    text = "Export",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal,
                    color = if (currentTab == 1) AppColors.NavTextActive else AppColors.NavInactive
                )
            },
            selected = currentTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.testTag("nav_tab_export"),
            selectedIconColor = AppColors.NavTextActive,
            unselectedIconColor = AppColors.NavInactive,
            selectedContentColor = AppColors.NavTextActive,
            unselectedContentColor = AppColors.NavInactive
        )
    }
}
