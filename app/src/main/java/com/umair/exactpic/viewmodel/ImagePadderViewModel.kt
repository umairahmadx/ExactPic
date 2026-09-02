package com.umair.exactpic.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umair.exactpic.engine.ImagePadderEngine
import com.umair.exactpic.engine.ProcessResult
import com.umair.exactpic.model.DimensionUnit
import com.umair.exactpic.model.ImageFormat
import com.umair.exactpic.model.ImageMetadata
import com.umair.exactpic.model.PaddingMethod
import com.umair.exactpic.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

data class ExportedImageItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val metadata: ImageMetadata,
    val bytes: ByteArray,
    val uri: Uri? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class PadderUiState(
    val originalMetadata: ImageMetadata? = null,
    val originalBytes: ByteArray? = null,
    val originalUri: Uri? = null,
    val targetSizeKb: String = "",
    val overrideWidth: String = "",
    val overrideHeight: String = "",
    val isAspectRatioLocked: Boolean = true,
    val isResizeEnabled: Boolean = false,
    val dimensionUnit: DimensionUnit = DimensionUnit.PIXELS,
    val dpi: Int = 300,
    val paddingMethod: PaddingMethod = PaddingMethod.AUTO,
    val isProcessing: Boolean = false,
    val processedBytes: ByteArray? = null,
    val processedMetadata: ImageMetadata? = null,
    val techniqueSummary: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val cameraTempUri: Uri? = null,
    val currentTab: Int = 0, // 0 = Canvas, 1 = Export
    val exportedImages: List<ExportedImageItem> = emptyList(),
    val scalePresetPercentage: Int = 100,
    val selectedQuickAdjustment: Int? = 0
) {
    val currentFileKb: Double
        get() = (originalMetadata?.sizeBytes ?: 0L) / 1024.0

    val targetKbDouble: Double?
        get() = targetSizeKb.toDoubleOrNull()

    val scaleFactorText: String
        get() {
            val cur = currentFileKb
            val target = targetKbDouble ?: return "1.00x"
            if (cur <= 0) return "1.00x"
            return String.format(Locale.US, "%.2fx", target / cur)
        }

    val growthPercentageText: String
        get() {
            val cur = currentFileKb
            val target = targetKbDouble ?: return "100% of Original"
            if (cur <= 0) return "100% of Original"
            val pct = ((target / cur) * 100.0).roundToInt()
            return "$pct% of Original"
        }

    val progressFraction: Float
        get() {
            val cur = currentFileKb
            val target = targetKbDouble ?: return 0.5f
            if (cur <= 0) return 0.5f
            val ratio = (target / cur).toFloat()
            // Map 1.0x -> 0.25, 3.42x -> ~0.85
            return (ratio / 4.0f).coerceIn(0.08f, 1.0f)
        }

    val isPaddingOperation: Boolean
        get() {
            val target = targetKbDouble ?: return false
            return target > currentFileKb
        }

    val isCompressOperation: Boolean
        get() {
            val target = targetKbDouble ?: return false
            return target < currentFileKb
        }

    val sizeDiffPercent: String
        get() {
            val orig = originalMetadata?.sizeBytes ?: return "0%"
            val proc = processedMetadata?.sizeBytes ?: return "0%"
            val diff = proc - orig
            val pct = (diff.toDouble() / orig.toDouble()) * 100.0
            return if (pct >= 0) "+%.1f%%".format(Locale.US, pct) else "%.1f%%".format(Locale.US, pct)
        }

    val sizeDiffKb: String
        get() {
            val orig = originalMetadata?.sizeBytes ?: return "0 KB"
            val proc = processedMetadata?.sizeBytes ?: return "0 KB"
            val diffBytes = proc - orig
            val diffKb = diffBytes / 1024.0
            return if (diffKb >= 0) "+%.2f KB".format(Locale.US, diffKb) else "%.2f KB".format(Locale.US, diffKb)
        }

    val computedPixelWidth: Int
        get() {
            val meta = originalMetadata ?: return 0
            if (!isResizeEnabled) return meta.width
            val w = overrideWidth.toDoubleOrNull() ?: return meta.width
            return dimensionUnit.toPixels(w, dpi, meta.width)
        }

    val computedPixelHeight: Int
        get() {
            val meta = originalMetadata ?: return 0
            if (!isResizeEnabled) return meta.height
            val h = overrideHeight.toDoubleOrNull() ?: return meta.height
            return dimensionUnit.toPixels(h, dpi, meta.height)
        }

    val effectiveDimensionsLabel: String
        get() = "$computedPixelWidth × $computedPixelHeight px"
}

class ImagePadderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PadderUiState())
    val uiState: StateFlow<PadderUiState> = _uiState.asStateFlow()

    init {
        // Pre-populate with standard sample exported gallery items matching reference screens
        viewModelScope.launch(Dispatchers.Default) {
            val (m1, b1) = ImageUtils.createMechWatchSample()
            val (m2, b2) = ImageUtils.createServerRoomSample()
            val (m3, b3) = ImageUtils.createAbstractGeoSample()
            val (m4, b4) = ImageUtils.createSciFiHudSample()

            val items = listOf(
                ExportedImageItem(fileName = "Mech_Watch_01.png", metadata = m1, bytes = b1),
                ExportedImageItem(fileName = "Server_Room_Deep.jpg", metadata = m2, bytes = b2),
                ExportedImageItem(fileName = "Abstract_Geo_Dark.png", metadata = m3, bytes = b3),
                ExportedImageItem(fileName = "Telemetry_HUD_04.png", metadata = m4, bytes = b4)
            )

            _uiState.update { it.copy(exportedImages = items) }
        }
    }

    fun switchTab(tabIndex: Int) {
        _uiState.update { it.copy(currentTab = tabIndex) }
    }

    fun selectScalePreset(percent: Int) {
        val origMeta = _uiState.value.originalMetadata ?: return
        val factor = percent / 100.0
        val targetW = (origMeta.width * factor).roundToInt().coerceAtLeast(1)
        val targetH = (origMeta.height * factor).roundToInt().coerceAtLeast(1)
        val state = _uiState.value
        val newW = state.dimensionUnit.fromPixels(targetW, state.dpi, origMeta.width)
        val newH = state.dimensionUnit.fromPixels(targetH, state.dpi, origMeta.height)
        _uiState.update {
            it.copy(
                scalePresetPercentage = percent,
                overrideWidth = state.dimensionUnit.formatValue(newW),
                overrideHeight = state.dimensionUnit.formatValue(newH)
            )
        }
    }

    fun applyQuickAdjustment(adjustment: Int?) {
        val origMeta = _uiState.value.originalMetadata ?: return
        val origKb = origMeta.sizeBytes / 1024.0
        val targetKb = when (adjustment) {
            null, 0 -> origKb
            else -> origKb * (1.0 + adjustment / 100.0)
        }
        val targetKbRounded = targetKb.roundToInt().coerceAtLeast(1)
        _uiState.update {
            it.copy(
                selectedQuickAdjustment = adjustment,
                targetSizeKb = targetKbRounded.toString()
            )
        }
    }

    fun clearLoadedImage() {
        _uiState.update {
            it.copy(
                originalBytes = null,
                originalMetadata = null,
                originalUri = null,
                processedBytes = null,
                processedMetadata = null,
                targetSizeKb = "",
                overrideWidth = "",
                overrideHeight = "",
                isResizeEnabled = false,
                techniqueSummary = null,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun resetAll() {
        val origMeta = _uiState.value.originalMetadata ?: return
        val origKb = (origMeta.sizeBytes / 1024.0).roundToInt()
        val state = _uiState.value
        val w = state.dimensionUnit.fromPixels(origMeta.width, state.dpi, origMeta.width)
        val h = state.dimensionUnit.fromPixels(origMeta.height, state.dpi, origMeta.height)
        _uiState.update {
            it.copy(
                targetSizeKb = origKb.toString(),
                selectedQuickAdjustment = 0,
                scalePresetPercentage = 100,
                overrideWidth = state.dimensionUnit.formatValue(w),
                overrideHeight = state.dimensionUnit.formatValue(h),
                isResizeEnabled = false,
                processedBytes = null,
                processedMetadata = null
            )
        }
    }

    fun downloadExportedItem(context: Context, item: ExportedImageItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            val res = withContext(Dispatchers.IO) {
                ImageUtils.saveImageToGallery(
                    context = context,
                    bytes = item.bytes,
                    mimeType = item.metadata.mimeType,
                    format = item.metadata.format,
                    suggestedName = item.fileName.substringBeforeLast(".")
                )
            }
            res.onSuccess {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        successMessage = "Saved ${item.fileName} to Gallery!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Download failed: ${err.message}"
                    )
                }
            }
        }
    }

    fun shareExportedItem(context: Context, item: ExportedImageItem): Intent? {
        val shareResult = ImageUtils.createShareIntent(
            context = context,
            bytes = item.bytes,
            mimeType = item.metadata.mimeType,
            fileName = item.fileName
        )
        return shareResult.getOrNull()
    }

    fun loadImageFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null, successMessage = null) }
            val result = withContext(Dispatchers.IO) {
                ImageUtils.readImageInfo(context, uri)
            }
            result.onSuccess { (meta, bytes) ->
                val currentKb = meta.sizeBytes / 1024.0
                // Default target: round up to nice KB or +100KB
                val defaultTargetKb = (currentKb * 1.25).roundToInt().coerceAtLeast((currentKb + 50).roundToInt())
                _uiState.update {
                    it.copy(
                        originalMetadata = meta,
                        originalBytes = bytes,
                        originalUri = uri,
                        targetSizeKb = defaultTargetKb.toString(),
                        overrideWidth = meta.width.toString(),
                        overrideHeight = meta.height.toString(),
                        isProcessing = false,
                        processedBytes = null,
                        processedMetadata = null,
                        techniqueSummary = null,
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Failed to load image: ${err.localizedMessage ?: err.message}"
                    )
                }
            }
        }
    }

    fun loadSample(format: ImageFormat) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null, successMessage = null) }
            val (meta, bytes) = withContext(Dispatchers.Default) {
                ImageUtils.createSampleImage(format)
            }
            val currentKb = meta.sizeBytes / 1024.0
            val defaultTargetKb = (currentKb * 1.5).roundToInt().coerceAtLeast((currentKb + 100).roundToInt())

            _uiState.update {
                it.copy(
                    originalMetadata = meta,
                    originalBytes = bytes,
                    originalUri = null,
                    targetSizeKb = defaultTargetKb.toString(),
                    overrideWidth = meta.width.toString(),
                    overrideHeight = meta.height.toString(),
                    isProcessing = false,
                    processedBytes = null,
                    processedMetadata = null,
                    techniqueSummary = null,
                    errorMessage = null,
                    successMessage = "Loaded sample ${format.displayName} image."
                )
            }
        }
    }

    fun prepareCameraCapture(context: Context): Uri {
        val (uri, _) = ImageUtils.createCameraTempUri(context)
        _uiState.update { it.copy(cameraTempUri = uri) }
        return uri
    }

    fun onCameraCaptureSuccess(context: Context) {
        val tempUri = _uiState.value.cameraTempUri ?: return
        loadImageFromUri(context, tempUri)
    }

    fun updateTargetSize(newKb: String) {
        _uiState.update { it.copy(targetSizeKb = newKb, errorMessage = null) }
    }

    fun adjustTargetPercentage(percentDelta: Int) {
        val current = _uiState.value.targetKbDouble?.takeIf { it > 0 } ?: _uiState.value.currentFileKb
        val target = (current * (1.0 + percentDelta / 100.0)).roundToInt().coerceAtLeast(1)
        _uiState.update { it.copy(targetSizeKb = target.toString(), errorMessage = null) }
    }

    fun setTargetToOriginal() {
        val origKb = _uiState.value.currentFileKb.roundToInt().coerceAtLeast(1)
        _uiState.update { it.copy(targetSizeKb = origKb.toString(), errorMessage = null) }
    }

    fun toggleResizeEnabled() {
        _uiState.update { it.copy(isResizeEnabled = !it.isResizeEnabled) }
    }

    fun setResizeEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isResizeEnabled = enabled) }
    }

    fun addSizePreset(deltaKb: Long) {
        val currentKb = _uiState.value.currentFileKb
        val target = (currentKb + deltaKb).roundToInt().coerceAtLeast(1)
        _uiState.update { it.copy(targetSizeKb = target.toString()) }
    }

    fun setExactTargetPreset(targetKb: Long) {
        _uiState.update { it.copy(targetSizeKb = targetKb.toString()) }
    }

    fun scaleTargetPreset(factor: Double) {
        val currentKb = _uiState.value.currentFileKb
        val target = (currentKb * factor).roundToInt().coerceAtLeast(1)
        _uiState.update { it.copy(targetSizeKb = target.toString()) }
    }

    fun setDimensionUnit(newUnit: DimensionUnit) {
        val state = _uiState.value
        val meta = state.originalMetadata ?: run {
            _uiState.update { it.copy(dimensionUnit = newUnit) }
            return
        }
        if (state.dimensionUnit == newUnit) return

        // Compute current pixel dimensions from current values
        val currentWVal = state.overrideWidth.toDoubleOrNull() ?: meta.width.toDouble()
        val currentHVal = state.overrideHeight.toDoubleOrNull() ?: meta.height.toDouble()
        val currentPxW = state.dimensionUnit.toPixels(currentWVal, state.dpi, meta.width)
        val currentPxH = state.dimensionUnit.toPixels(currentHVal, state.dpi, meta.height)

        // Convert to the new unit
        val newW = newUnit.fromPixels(currentPxW, state.dpi, meta.width)
        val newH = newUnit.fromPixels(currentPxH, state.dpi, meta.height)

        _uiState.update {
            it.copy(
                dimensionUnit = newUnit,
                overrideWidth = newUnit.formatValue(newW),
                overrideHeight = newUnit.formatValue(newH),
                errorMessage = null
            )
        }
    }

    fun setDpi(newDpi: Int) {
        val state = _uiState.value
        if (state.dpi == newDpi) return
        _uiState.update { it.copy(dpi = newDpi) }
    }

    fun updateWidth(newWidthStr: String) {
        val state = _uiState.value
        val origMeta = state.originalMetadata
        val newWidthVal = newWidthStr.toDoubleOrNull()

        if (state.isAspectRatioLocked && origMeta != null && newWidthVal != null && origMeta.width > 0 && origMeta.height > 0) {
            val aspect = origMeta.height.toDouble() / origMeta.width.toDouble()
            val computedHeightVal = newWidthVal * aspect
            _uiState.update {
                it.copy(
                    overrideWidth = newWidthStr,
                    overrideHeight = state.dimensionUnit.formatValue(computedHeightVal),
                    errorMessage = null
                )
            }
        } else {
            _uiState.update { it.copy(overrideWidth = newWidthStr, errorMessage = null) }
        }
    }

    fun updateHeight(newHeightStr: String) {
        val state = _uiState.value
        val origMeta = state.originalMetadata
        val newHeightVal = newHeightStr.toDoubleOrNull()

        if (state.isAspectRatioLocked && origMeta != null && newHeightVal != null && origMeta.width > 0 && origMeta.height > 0) {
            val aspect = origMeta.width.toDouble() / origMeta.height.toDouble()
            val computedWidthVal = newHeightVal * aspect
            _uiState.update {
                it.copy(
                    overrideHeight = newHeightStr,
                    overrideWidth = state.dimensionUnit.formatValue(computedWidthVal),
                    errorMessage = null
                )
            }
        } else {
            _uiState.update { it.copy(overrideHeight = newHeightStr, errorMessage = null) }
        }
    }

    fun setScalePreset(percent: Int) {
        val origMeta = _uiState.value.originalMetadata ?: return
        val state = _uiState.value
        val factor = percent / 100.0
        val targetPxW = (origMeta.width * factor).roundToInt().coerceAtLeast(1)
        val targetPxH = (origMeta.height * factor).roundToInt().coerceAtLeast(1)

        val newW = state.dimensionUnit.fromPixels(targetPxW, state.dpi, origMeta.width)
        val newH = state.dimensionUnit.fromPixels(targetPxH, state.dpi, origMeta.height)

        _uiState.update {
            it.copy(
                overrideWidth = state.dimensionUnit.formatValue(newW),
                overrideHeight = state.dimensionUnit.formatValue(newH)
            )
        }
    }

    fun setDimensionsPreset(wInUnit: Double, hInUnit: Double, unit: DimensionUnit) {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                dimensionUnit = unit,
                overrideWidth = unit.formatValue(wInUnit),
                overrideHeight = unit.formatValue(hInUnit),
                isAspectRatioLocked = false
            )
        }
    }

    fun toggleAspectRatioLock() {
        _uiState.update { it.copy(isAspectRatioLocked = !it.isAspectRatioLocked) }
    }

    fun setPaddingMethod(method: PaddingMethod) {
        _uiState.update { it.copy(paddingMethod = method) }
    }

    fun resetDimensions() {
        val orig = _uiState.value.originalMetadata ?: return
        val state = _uiState.value
        val w = state.dimensionUnit.fromPixels(orig.width, state.dpi, orig.width)
        val h = state.dimensionUnit.fromPixels(orig.height, state.dpi, orig.height)
        _uiState.update {
            it.copy(
                overrideWidth = state.dimensionUnit.formatValue(w),
                overrideHeight = state.dimensionUnit.formatValue(h)
            )
        }
    }

    fun processImage() {
        val state = _uiState.value
        val bytes = state.originalBytes
        val meta = state.originalMetadata

        if (bytes == null || meta == null) {
            _uiState.update { it.copy(errorMessage = "Please select or capture an image first.") }
            return
        }

        val targetKb = state.targetSizeKb.toDoubleOrNull()
        if (targetKb == null || targetKb <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid positive Target File Size in KB.") }
            return
        }

        val targetW = if (state.isResizeEnabled) {
            val doubleW = state.overrideWidth.toDoubleOrNull()
            if (doubleW == null || doubleW <= 0) {
                _uiState.update { it.copy(errorMessage = "Please enter a valid positive width.") }
                return
            }
            state.dimensionUnit.toPixels(doubleW, state.dpi, meta.width)
        } else {
            meta.width
        }

        val targetH = if (state.isResizeEnabled) {
            val doubleH = state.overrideHeight.toDoubleOrNull()
            if (doubleH == null || doubleH <= 0) {
                _uiState.update { it.copy(errorMessage = "Please enter a valid positive height.") }
                return
            }
            state.dimensionUnit.toPixels(doubleH, state.dpi, meta.height)
        } else {
            meta.height
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null, successMessage = null) }

            val result = withContext(Dispatchers.Default) {
                ImagePadderEngine.process(
                    originalBytes = bytes,
                    originalMetadata = meta,
                    targetSizeKb = targetKb,
                    targetWidth = targetW,
                    targetHeight = targetH,
                    preferredMethod = state.paddingMethod
                )
            }

            when (result) {
                is ProcessResult.Success -> {
                    val newExported = ExportedImageItem(
                        fileName = result.outputMetadata.fileName,
                        metadata = result.outputMetadata,
                        bytes = result.outputBytes
                    )
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            processedBytes = result.outputBytes,
                            processedMetadata = result.outputMetadata,
                            techniqueSummary = result.techniqueSummary,
                            successMessage = "Image processed successfully!",
                            exportedImages = listOf(newExported) + it.exportedImages
                        )
                    }
                }
                is ProcessResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun saveToGallery(context: Context) {
        val state = _uiState.value
        val bytes = state.processedBytes
        val meta = state.processedMetadata

        if (bytes == null || meta == null) {
            _uiState.update { it.copy(errorMessage = "No processed image to save. Process an image first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null, successMessage = null) }
            val saveResult = withContext(Dispatchers.IO) {
                ImageUtils.saveImageToGallery(
                    context = context,
                    bytes = bytes,
                    mimeType = meta.mimeType,
                    format = meta.format,
                    suggestedName = "padded_${meta.width}x${meta.height}"
                )
            }

            saveResult.onSuccess { uri ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        successMessage = "Saved to Gallery (Pictures/ImagePadder)!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Save failed: ${err.localizedMessage ?: err.message}"
                    )
                }
            }
        }
    }

    fun createShareIntent(context: Context): Intent? {
        val state = _uiState.value
        val bytes = state.processedBytes
        val meta = state.processedMetadata

        if (bytes == null || meta == null) {
            _uiState.update { it.copy(errorMessage = "No processed image available to share.") }
            return null
        }

        val shareResult = ImageUtils.createShareIntent(
            context = context,
            bytes = bytes,
            mimeType = meta.mimeType,
            fileName = "padded_image_${System.currentTimeMillis()}.${meta.format.extension}"
        )

        return shareResult.getOrNull()
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
