package com.umair.exactpic.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umair.exactpic.model.ImageFormat
import com.umair.exactpic.ui.components.AppBrandIcon
import com.umair.exactpic.ui.components.AppLogo
import com.umair.exactpic.ui.components.EmptyCanvasCard
import com.umair.exactpic.ui.components.ExportedImagesSection
import com.umair.exactpic.ui.components.PadderBottomBar
import com.umair.exactpic.ui.components.ProcessedResultCard
import com.umair.exactpic.ui.components.ResizeOptionCard
import com.umair.exactpic.ui.components.SourceImageSection
import com.umair.exactpic.ui.components.StandardSamplesRow
import com.umair.exactpic.ui.components.TargetSizeCard
import com.umair.exactpic.ui.components.bounceClick
import com.umair.exactpic.ui.theme.AppColors
import com.umair.exactpic.viewmodel.ImagePadderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePadderScreen(
    viewModel: ImagePadderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.loadImageFromUri(context, uri)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.onCameraCaptureSuccess(context)
        }
    }

    // Handle snackbar messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    // System back button handling to redirect to image selection screen
    BackHandler(enabled = uiState.originalBytes != null || uiState.currentTab != 0) {
        if (uiState.currentTab != 0) {
            viewModel.switchTab(0)
        } else if (uiState.originalBytes != null) {
            viewModel.clearLoadedImage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.BackgroundDark),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (uiState.originalBytes != null && uiState.currentTab == 0) {
                        IconButton(
                            onClick = { viewModel.clearLoadedImage() },
                            modifier = Modifier.testTag("app_bar_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to image selection",
                                tint = AppColors.IconWhite
                            )
                        }
                    } else if (uiState.currentTab != 0) {
                        IconButton(
                            onClick = { viewModel.switchTab(0) },
                            modifier = Modifier.testTag("app_bar_back_to_canvas_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Canvas",
                                tint = AppColors.IconWhite
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 10.dp)
                                .clickable { viewModel.switchTab(0) }
                        ) {
                            AppLogo()
                        }
                    }
                },
                title = {
                    Text(
                        text = if (uiState.currentTab == 0) "ExactPic" else "Exported Images",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.TopBarBackground,
                    titleContentColor = AppColors.TextPrimary
                )
            )
        },
        bottomBar = {
            PadderBottomBar(
                currentTab = uiState.currentTab,
                onTabSelected = { viewModel.switchTab(it) }
            )
        }
    ) { innerPadding ->
        Crossfade(
            targetState = uiState.currentTab,
            label = "tabCrossfade",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AppColors.BackgroundDark)
        ) { tab ->
            when (tab) {
                0 -> {
                    // Canvas Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 640.dp)
                        ) {
                            if (uiState.originalBytes == null) {
                                // Screen 1: Empty state
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    EmptyCanvasCard(
                                        onPickGallery = {
                                            galleryLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        onTakePhoto = {
                                            val tempUri = viewModel.prepareCameraCapture(context)
                                            cameraLauncher.launch(tempUri)
                                        }
                                    )

                                    StandardSamplesRow(
                                        onSelectSample = { format -> viewModel.loadSample(format) }
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            } else {
                                // Screen 2: Loaded state
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // 1. Source Image Section
                                    SourceImageSection(
                                        uiState = uiState,
                                        onChangeImage = {
                                            galleryLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    )

                                    // 2. Target File Size Card
                                    TargetSizeCard(
                                        uiState = uiState,
                                        onTargetSizeChange = { viewModel.updateTargetSize(it) },
                                        onQuickAdjustment = { viewModel.applyQuickAdjustment(it) }
                                    )

                                    // 3. Resize & Dimensions Card
                                    ResizeOptionCard(
                                        uiState = uiState,
                                        onToggleResize = { viewModel.toggleResizeEnabled() },
                                        onUnitChange = { viewModel.setDimensionUnit(it) },
                                        onWidthChange = { viewModel.updateWidth(it) },
                                        onHeightChange = { viewModel.updateHeight(it) },
                                        onToggleLock = { viewModel.toggleAspectRatioLock() },
                                        onScalePreset = { viewModel.selectScalePreset(it) },
                                        onDimensionsPreset = { w, h, unit -> viewModel.setDimensionsPreset(w, h, unit) }
                                    )

                                    // 4. Action Buttons Row: Reset and Apply Changes
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Reset Button (Dark)
                                        Box(
                                            modifier = Modifier
                                                .width(100.dp)
                                                .height(54.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(AppColors.DarkPillBackground)
                                                .border(1.dp, AppColors.DarkPillBorder, RoundedCornerShape(14.dp))
                                                .bounceClick(scaleDown = 0.95f) { viewModel.resetAll() }
                                                .testTag("reset_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Reset",
                                                color = AppColors.TextPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        // Apply Changes Button (White)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(54.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(AppColors.ActivePillBackground)
                                                .bounceClick(scaleDown = 0.96f) { viewModel.processImage() }
                                                .testTag("apply_changes_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (uiState.isProcessing) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(20.dp),
                                                        color = AppColors.IconDark,
                                                        strokeWidth = 2.dp
                                                    )
                                                    Text(
                                                        text = "Processing...",
                                                        color = AppColors.TextDark,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = "Apply Changes",
                                                    color = AppColors.TextDark,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }

                                    // 5. Processed Result Card (if generated)
                                    AnimatedVisibility(
                                        visible = uiState.processedMetadata != null && uiState.processedBytes != null,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        if (uiState.processedMetadata != null && uiState.processedBytes != null) {
                                            ProcessedResultCard(
                                                uiState = uiState,
                                                onSaveToGallery = { viewModel.saveToGallery(context) },
                                                onShare = {
                                                    val intent = viewModel.createShareIntent(context)
                                                    if (intent != null) {
                                                        context.startActivity(
                                                            Intent.createChooser(intent, "Share Image")
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Screen 3: Export Tab
                    ExportedImagesSection(
                        exportedImages = uiState.exportedImages,
                        onDownload = { viewModel.downloadExportedItem(context, it) },
                        onShare = { item ->
                            val intent = viewModel.shareExportedItem(context, item)
                            if (intent != null) {
                                context.startActivity(Intent.createChooser(intent, "Share Exported Image"))
                            }
                        }
                    )
                }
            }
        }
    }
}
