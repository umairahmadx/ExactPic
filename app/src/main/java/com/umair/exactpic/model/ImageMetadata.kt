package com.umair.exactpic.model

import java.util.Locale
import kotlin.math.roundToInt

enum class DimensionUnit(
    val id: String,
    val label: String,
    val symbol: String
) {
    PIXELS("px", "Pixels", "px"),
    INCHES("in", "Inches", "inch"),
    CENTIMETERS("cm", "Centimeters", "cm"),
    MILLIMETERS("mm", "Millimeters", "mm"),
    PERCENT("percent", "Percent", "%");

    fun toPixels(value: Double, dpi: Int, basePixels: Int): Int {
        return when (this) {
            PIXELS -> value.roundToInt()
            INCHES -> (value * dpi).roundToInt()
            CENTIMETERS -> (value * dpi / 2.54).roundToInt()
            MILLIMETERS -> (value * dpi / 25.4).roundToInt()
            PERCENT -> (basePixels * (value / 100.0)).roundToInt()
        }.coerceAtLeast(1)
    }

    fun fromPixels(pixels: Int, dpi: Int, basePixels: Int): Double {
        return when (this) {
            PIXELS -> pixels.toDouble()
            INCHES -> pixels.toDouble() / dpi
            CENTIMETERS -> (pixels.toDouble() * 2.54) / dpi
            MILLIMETERS -> (pixels.toDouble() * 25.4) / dpi
            PERCENT -> if (basePixels > 0) (pixels.toDouble() / basePixels) * 100.0 else 100.0
        }
    }

    fun formatValue(value: Double): String {
        return when (this) {
            PIXELS -> value.roundToInt().toString()
            INCHES, CENTIMETERS -> {
                val formatted = String.format(Locale.US, "%.2f", value)
                if (formatted.contains('.')) {
                    formatted.trimEnd('0').trimEnd('.')
                } else {
                    formatted
                }
            }
            MILLIMETERS -> {
                val formatted = String.format(Locale.US, "%.1f", value)
                if (formatted.contains('.')) {
                    formatted.trimEnd('0').trimEnd('.')
                } else {
                    formatted
                }
            }
            PERCENT -> String.format(Locale.US, "%.0f", value)
        }
    }
}

enum class ImageFormat(val extension: String, val mimeType: String, val displayName: String) {
    JPEG("jpg", "image/jpeg", "JPEG / JPG"),
    PNG("png", "image/png", "PNG"),
    WEBP("webp", "image/webp", "WebP"),
    UNKNOWN("bin", "application/octet-stream", "Unknown");

    companion object {
        fun fromMimeOrBytes(mime: String?, bytes: ByteArray? = null): ImageFormat {
            if (mime != null) {
                when {
                    mime.contains("jpeg", ignoreCase = true) || mime.contains("jpg", ignoreCase = true) -> return JPEG
                    mime.contains("png", ignoreCase = true) -> return PNG
                    mime.contains("webp", ignoreCase = true) -> return WEBP
                }
            }
            if (bytes != null && bytes.size >= 8) {
                // JPEG: FF D8 FF
                if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
                    return JPEG
                }
                // PNG: 89 50 4E 47 0D 0A 1A 0A
                if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()) {
                    return PNG
                }
                // WEBP: RIFF....WEBP
                if (bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() && bytes[2] == 'F'.code.toByte() && bytes[3] == 'F'.code.toByte()) {
                    if (bytes.size >= 12 && bytes[8] == 'W'.code.toByte() && bytes[9] == 'E'.code.toByte() && bytes[10] == 'B'.code.toByte() && bytes[11] == 'P'.code.toByte()) {
                        return WEBP
                    }
                }
            }
            return UNKNOWN
        }
    }
}

enum class PaddingMethod(val label: String, val description: String) {
    AUTO("Smart Auto", "Optimal method for image format (COM/tEXt)"),
    COM_MARKER("JPEG COM Marker", "Injects standard 0xFFFE comment metadata chunk"),
    TRAILING_EOI("Trailing Bytes", "Appends bytes safely after image terminator (EOI/IEND)"),
    PNG_TEXT_CHUNK("PNG tEXt Chunk", "Inserts ancillary 'tEXt' chunk with valid CRC32")
}

data class ImageMetadata(
    val fileName: String,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val format: ImageFormat
) {
    val formattedBytes: String
        get() = String.format(Locale.US, "%,d B", sizeBytes)

    val formattedKB: String
        get() = String.format(Locale.US, "%.2f KB", sizeBytes / 1024.0)

    val formattedMB: String
        get() = String.format(Locale.US, "%.2f MB", sizeBytes / (1024.0 * 1024.0))

    val sizeSummary: String
        get() = "$formattedBytes ($formattedKB / $formattedMB)"

    val dimensionsSummary: String
        get() = "${width} × ${height} px"

    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 1f

    val aspectRatioLabel: String
        get() {
            if (width <= 0 || height <= 0) return "1:1"
            val gcdVal = gcd(width, height)
            val rw = width / gcdVal
            val rh = height / gcdVal
            return if (rw < 100 && rh < 100) "$rw:$rh" else String.format(Locale.US, "%.2f:1", aspectRatio)
        }

    private fun gcd(a: Int, b: Int): Int {
        var x = a
        var y = b
        while (y != 0) {
            val t = y
            y = x % y
            x = t
        }
        return if (x > 0) x else 1
    }
}
