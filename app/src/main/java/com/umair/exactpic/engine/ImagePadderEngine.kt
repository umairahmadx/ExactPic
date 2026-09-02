package com.umair.exactpic.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.umair.exactpic.model.ImageFormat
import com.umair.exactpic.model.ImageMetadata
import com.umair.exactpic.model.PaddingMethod
import java.io.ByteArrayOutputStream
import java.util.zip.CRC32
import kotlin.math.max
import kotlin.math.min

sealed class ProcessResult {
    data class Success(
        val outputBytes: ByteArray,
        val outputMetadata: ImageMetadata,
        val techniqueSummary: String,
        val exactSizeMatched: Boolean,
        val targetBytes: Long
    ) : ProcessResult()

    data class Error(val message: String) : ProcessResult()
}

object ImagePadderEngine {

    /**
     * Main processing entry point.
     * Takes original bytes, optional dimension overrides, target KB, and padding options.
     */
    fun process(
        originalBytes: ByteArray,
        originalMetadata: ImageMetadata,
        targetSizeKb: Double?,
        targetWidth: Int?,
        targetHeight: Int?,
        preferredMethod: PaddingMethod = PaddingMethod.AUTO
    ): ProcessResult {
        try {
            if (originalBytes.isEmpty()) {
                return ProcessResult.Error("No image data provided.")
            }

            val currentSizeBytes = originalBytes.size.toLong()
            val format = originalMetadata.format
            val finalWidth = targetWidth ?: originalMetadata.width
            val finalHeight = targetHeight ?: originalMetadata.height

            if (finalWidth <= 0 || finalHeight <= 0) {
                return ProcessResult.Error("Invalid dimensions: Width and Height must be positive integers.")
            }

            val dimensionsChanged = (finalWidth != originalMetadata.width || finalHeight != originalMetadata.height)

            // If no target KB is specified, we either just resize or keep original
            val targetBytes = if (targetSizeKb != null && targetSizeKb > 0) {
                (targetSizeKb * 1024.0).toLong()
            } else {
                currentSizeBytes
            }

            // CASE 1: No dimension change and target size is LARGER than current -> Pure Binary Padding (zero quality loss!)
            if (!dimensionsChanged && targetBytes > currentSizeBytes) {
                val padDifference = targetBytes - currentSizeBytes
                return padBinary(
                    bytes = originalBytes,
                    format = format,
                    targetBytes = targetBytes,
                    padDifference = padDifference,
                    width = finalWidth,
                    height = finalHeight,
                    preferredMethod = preferredMethod
                )
            }

            // CASE 2: Dimensions changed OR Target size is LESS than or equal to current size
            // We need to re-encode / compress the image.
            val decodedBitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
                ?: return ProcessResult.Error("Failed to decode image data into bitmap.")

            val processedBitmap = if (dimensionsChanged) {
                Bitmap.createScaledBitmap(decodedBitmap, finalWidth, finalHeight, true)
            } else {
                decodedBitmap
            }

            // Re-encode with compression
            val compressFormat = when (format) {
                ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
                ImageFormat.PNG -> Bitmap.CompressFormat.PNG
                ImageFormat.WEBP -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        Bitmap.CompressFormat.WEBP_LOSSY
                    } else {
                        @Suppress("DEPRECATION")
                        Bitmap.CompressFormat.WEBP
                    }
                }
                ImageFormat.UNKNOWN -> Bitmap.CompressFormat.JPEG
            }

            val compressedResult = compressToTarget(
                bitmap = processedBitmap,
                format = compressFormat,
                targetBytes = targetBytes
            )

            // If compressed result is smaller than targetBytes, pad it up to targetBytes!
            val finalBytes = if (compressedResult.bytes.size < targetBytes) {
                padBinaryRaw(
                    bytes = compressedResult.bytes,
                    format = format,
                    targetBytes = targetBytes,
                    preferredMethod = preferredMethod
                )
            } else {
                compressedResult.bytes
            }

            val newMetadata = ImageMetadata(
                fileName = "processed_${originalMetadata.fileName}",
                mimeType = originalMetadata.mimeType,
                width = finalWidth,
                height = finalHeight,
                sizeBytes = finalBytes.size.toLong(),
                format = format
            )

            val explanation = buildString {
                if (dimensionsChanged) {
                    append("Resized dimensions from ${originalMetadata.width}×${originalMetadata.height} to ${finalWidth}×${finalHeight} px. ")
                }
                if (compressedResult.qualityUsed != null) {
                    append("Re-encoded with quality ${compressedResult.qualityUsed}%. ")
                }
                if (finalBytes.size.toLong() == targetBytes) {
                    append("Padded to exact target: %,d bytes (%.2f KB).".format(targetBytes, targetBytes / 1024.0))
                } else if (finalBytes.size.toLong() > targetBytes) {
                    append("Minimum achievable size at this resolution is %,d bytes (%.2f KB). Consider reducing dimensions for smaller size.".format(finalBytes.size, finalBytes.size / 1024.0))
                }
            }

            return ProcessResult.Success(
                outputBytes = finalBytes,
                outputMetadata = newMetadata,
                techniqueSummary = explanation,
                exactSizeMatched = finalBytes.size.toLong() == targetBytes,
                targetBytes = targetBytes
            )
        } catch (e: Exception) {
            return ProcessResult.Error("Error during image processing: ${e.localizedMessage ?: e.message}")
        }
    }

    /**
     * Pure binary padding without re-encoding, preserving 100% pixel fidelity.
     */
    private fun padBinary(
        bytes: ByteArray,
        format: ImageFormat,
        targetBytes: Long,
        padDifference: Long,
        width: Int,
        height: Int,
        preferredMethod: PaddingMethod
    ): ProcessResult {
        val (paddedBytes, technique) = when (format) {
            ImageFormat.JPEG -> {
                when (preferredMethod) {
                    PaddingMethod.TRAILING_EOI -> {
                        Pair(padJpegEoi(bytes, targetBytes), "Appended ${formatBytes(padDifference)} harmless trailing bytes after EOI marker (0xFFD9).")
                    }
                    else -> {
                        // Default to COM Marker or Auto
                        val res = padJpegComMarker(bytes, targetBytes)
                        Pair(res, "Injected standard COM comment marker (0xFFFE) with ${formatBytes(padDifference)} padding bytes. Decodes seamlessly with 0% pixel loss.")
                    }
                }
            }
            ImageFormat.PNG -> {
                val res = padPngTextChunk(bytes, targetBytes)
                Pair(res, "Inserted standard PNG 'tEXt' ancillary chunk before IEND with valid CRC32. Compliant with PNG specs; visual pixels 100% untouched.")
            }
            else -> {
                // WebP or Unknown
                val res = padTrailing(bytes, targetBytes)
                Pair(res, "Appended ${formatBytes(padDifference)} safe trailing padding bytes to reach exact target size.")
            }
        }

        val meta = ImageMetadata(
            fileName = "padded_image.${format.extension}",
            mimeType = format.mimeType,
            width = width,
            height = height,
            sizeBytes = paddedBytes.size.toLong(),
            format = format
        )

        return ProcessResult.Success(
            outputBytes = paddedBytes,
            outputMetadata = meta,
            techniqueSummary = technique,
            exactSizeMatched = paddedBytes.size.toLong() == targetBytes,
            targetBytes = targetBytes
        )
    }

    private fun padBinaryRaw(
        bytes: ByteArray,
        format: ImageFormat,
        targetBytes: Long,
        preferredMethod: PaddingMethod
    ): ByteArray {
        if (bytes.size.toLong() >= targetBytes) return bytes
        return when (format) {
            ImageFormat.JPEG -> {
                if (preferredMethod == PaddingMethod.TRAILING_EOI) {
                    padJpegEoi(bytes, targetBytes)
                } else {
                    padJpegComMarker(bytes, targetBytes)
                }
            }
            ImageFormat.PNG -> padPngTextChunk(bytes, targetBytes)
            else -> padTrailing(bytes, targetBytes)
        }
    }

    /**
     * JPEG COM Marker (0xFF 0xFE) injection:
     * Inserts COM markers right after the 2-byte SOI (0xFF 0xD8) header.
     * If padding needed is < 4 bytes, falls back to appending trailing bytes after EOI.
     */
    fun padJpegComMarker(bytes: ByteArray, targetBytes: Long): ByteArray {
        val needed = targetBytes - bytes.size
        if (needed <= 0) return bytes

        // Check if starts with SOI (0xFF, 0xD8)
        val hasSoi = bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()
        if (!hasSoi || needed < 4) {
            return padJpegEoi(bytes, targetBytes)
        }

        val out = ByteArrayOutputStream((targetBytes).toInt().coerceAtLeast(bytes.size))

        // Write SOI (2 bytes)
        out.write(bytes[0].toInt())
        out.write(bytes[1].toInt())

        // Inject COM markers
        var remainingPad = needed
        while (remainingPad >= 4) {
            // Marker overhead is 4 bytes: 0xFF, 0xFE, len_hi, len_lo
            // Maximum payload per COM marker is 65533 bytes (since 65533 + 2 <= 65535)
            val payloadSize = min(remainingPad - 4, 65533L).toInt()
            val markerLength = payloadSize + 2

            out.write(0xFF)
            out.write(0xFE)
            out.write((markerLength ushr 8) and 0xFF)
            out.write(markerLength and 0xFF)

            // Write dummy comment payload
            if (payloadSize > 0) {
                val dummy = ByteArray(payloadSize)
                out.write(dummy)
            }

            remainingPad -= (payloadSize + 4)
        }

        // Write the rest of original JPEG
        out.write(bytes, 2, bytes.size - 2)

        // If there's 1-3 leftover bytes that couldn't fit in a 4-byte COM marker, append after EOI
        if (remainingPad > 0) {
            val leftover = ByteArray(remainingPad.toInt())
            out.write(leftover)
        }

        return out.toByteArray()
    }

    /**
     * JPEG Trailing EOI padding:
     * Appends harmless trailing bytes after EOI (0xFF 0xD9).
     */
    fun padJpegEoi(bytes: ByteArray, targetBytes: Long): ByteArray {
        val needed = targetBytes - bytes.size
        if (needed <= 0) return bytes

        val out = ByteArrayOutputStream(targetBytes.toInt())
        out.write(bytes)
        val padding = ByteArray(needed.toInt())
        out.write(padding)
        return out.toByteArray()
    }

    /**
     * PNG 'tEXt' Ancillary Chunk insertion:
     * Parses chunks to find IEND chunk, then inserts a 'tEXt' chunk with valid CRC32 right before IEND.
     * Format:
     * [4 bytes Length N]
     * [4 bytes "tEXt"]
     * [N bytes Data: Keyword + null + text]
     * [4 bytes CRC32]
     */
    fun padPngTextChunk(bytes: ByteArray, targetBytes: Long): ByteArray {
        val needed = targetBytes - bytes.size
        if (needed <= 0) return bytes

        // PNG signature is 8 bytes: 89 50 4E 47 0D 0A 1A 0A
        val isPng = bytes.size >= 8 &&
                bytes[0] == 0x89.toByte() &&
                bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x4E.toByte() &&
                bytes[3] == 0x47.toByte()

        if (!isPng) {
            return padTrailing(bytes, targetBytes)
        }

        // Minimum 'tEXt' chunk size is 12 (overhead) + 2 (1-char key + 0x00) = 14 bytes
        if (needed < 14) {
            // Append trailing padding after IEND
            return padTrailing(bytes, targetBytes)
        }

        // Locate IEND chunk offset
        var iendOffset = -1
        var offset = 8
        while (offset + 8 <= bytes.size) {
            val length = readInt(bytes, offset)
            val type = String(bytes, offset + 4, 4, Charsets.US_ASCII)
            if (type == "IEND") {
                iendOffset = offset
                break
            }
            // Length + Type (4) + Data (length) + CRC (4)
            val chunkTotal = 4 + 4 + length + 4
            if (chunkTotal < 0 || offset + chunkTotal > bytes.size) break
            offset += chunkTotal
        }

        if (iendOffset == -1) {
            // Fallback: search backwards for IEND signature (49 45 4E 44)
            for (i in bytes.size - 12 downTo 8) {
                if (bytes[i] == 'I'.code.toByte() &&
                    bytes[i + 1] == 'E'.code.toByte() &&
                    bytes[i + 2] == 'N'.code.toByte() &&
                    bytes[i + 3] == 'D'.code.toByte()
                ) {
                    iendOffset = i - 4 // Length starts 4 bytes before type
                    break
                }
            }
        }

        if (iendOffset == -1) {
            return padTrailing(bytes, targetBytes)
        }

        // Construct 'tEXt' chunk
        // Total chunk overhead = 4 (length) + 4 ("tEXt") + 4 (crc) = 12 bytes
        val dataLength = (needed - 12).toInt()
        val typeBytes = "tEXt".toByteArray(Charsets.US_ASCII)

        // Keyword "Pad" + null byte = 4 bytes
        val keywordBytes = "Pad".toByteArray(Charsets.ISO_8859_1)
        val chunkData = ByteArray(dataLength)
        System.arraycopy(keywordBytes, 0, chunkData, 0, min(keywordBytes.size, dataLength))
        if (dataLength > keywordBytes.size) {
            chunkData[keywordBytes.size] = 0 // null separator
        }

        // Calculate CRC32 over Type + Data
        val crc = CRC32()
        crc.update(typeBytes)
        crc.update(chunkData)
        val crcValue = crc.value.toInt()

        val out = ByteArrayOutputStream(targetBytes.toInt())
        // 1. Write everything up to IEND
        out.write(bytes, 0, iendOffset)

        // 2. Write our 'tEXt' chunk
        writeInt(out, dataLength)
        out.write(typeBytes)
        out.write(chunkData)
        writeInt(out, crcValue)

        // 3. Write IEND chunk and anything after it
        out.write(bytes, iendOffset, bytes.size - iendOffset)

        return out.toByteArray()
    }

    private fun padTrailing(bytes: ByteArray, targetBytes: Long): ByteArray {
        val needed = targetBytes - bytes.size
        if (needed <= 0) return bytes
        val out = ByteArrayOutputStream(targetBytes.toInt())
        out.write(bytes)
        out.write(ByteArray(needed.toInt()))
        return out.toByteArray()
    }

    private fun readInt(bytes: ByteArray, offset: Int): Int {
        return ((bytes[offset].toInt() and 0xFF) shl 24) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
                (bytes[offset + 3].toInt() and 0xFF)
    }

    private fun writeInt(out: ByteArrayOutputStream, value: Int) {
        out.write((value ushr 24) and 0xFF)
        out.write((value ushr 16) and 0xFF)
        out.write((value ushr 8) and 0xFF)
        out.write(value and 0xFF)
    }

    private data class CompressionResult(val bytes: ByteArray, val qualityUsed: Int?)

    /**
     * Binary search compression quality to match target size.
     */
    private fun compressToTarget(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        targetBytes: Long
    ): CompressionResult {
        if (format == Bitmap.CompressFormat.PNG) {
            val out = ByteArrayOutputStream()
            bitmap.compress(format, 100, out)
            return CompressionResult(out.toByteArray(), qualityUsed = null)
        }

        var low = 1
        var high = 100
        var bestBytes: ByteArray? = null
        var bestQuality = 100

        // Binary search for highest quality where size <= targetBytes
        while (low <= high) {
            val mid = (low + high) / 2
            val out = ByteArrayOutputStream()
            bitmap.compress(format, mid, out)
            val currentArray = out.toByteArray()

            if (currentArray.size <= targetBytes) {
                bestBytes = currentArray
                bestQuality = mid
                low = mid + 1 // Try higher quality
            } else {
                high = mid - 1 // Reduce quality
            }
        }

        return if (bestBytes != null) {
            CompressionResult(bestBytes, bestQuality)
        } else {
            // Even quality 1 is larger than targetBytes
            val out = ByteArrayOutputStream()
            bitmap.compress(format, 1, out)
            CompressionResult(out.toByteArray(), 1)
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format(java.util.Locale.US, "%.2f KB", bytes / 1024.0)
            else -> "$bytes bytes"
        }
    }
}
