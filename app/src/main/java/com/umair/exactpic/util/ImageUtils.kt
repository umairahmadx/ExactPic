package com.umair.exactpic.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.umair.exactpic.model.ImageFormat
import com.umair.exactpic.model.ImageMetadata
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * Reads the image metadata and full raw byte array from a content URI.
     */
    fun readImageInfo(context: Context, uri: Uri): Result<Pair<ImageMetadata, ByteArray>> {
        return runCatching {
            val contentResolver = context.contentResolver

            // Determine file name
            var fileName = "image"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val n = cursor.getString(nameIndex)
                    if (!n.isNullOrBlank()) fileName = n
                }
            }

            // Read raw bytes
            val bytes = contentResolver.openInputStream(uri)?.use { inputStream ->
                readAllBytes(inputStream)
            } ?: throw IllegalStateException("Could not open input stream from URI: $uri")

            if (bytes.isEmpty()) {
                throw IllegalStateException("Selected file is empty (0 bytes).")
            }

            // Determine MIME type
            var mime = contentResolver.getType(uri)
            if (mime.isNullOrBlank()) {
                mime = when {
                    fileName.endsWith(".png", ignoreCase = true) -> "image/png"
                    fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                    fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
                    else -> null
                }
            }

            val format = ImageFormat.fromMimeOrBytes(mime, bytes)

            // Decode dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

            var width = options.outWidth
            var height = options.outHeight

            // If bounds decoding returned 0 or -1, try full decode
            if (width <= 0 || height <= 0) {
                val fullBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (fullBmp != null) {
                    width = fullBmp.width
                    height = fullBmp.height
                } else {
                    width = 800
                    height = 600
                }
            }

            val resolvedMime = if (!mime.isNullOrBlank()) mime else format.mimeType

            val meta = ImageMetadata(
                fileName = fileName,
                mimeType = resolvedMime,
                width = width,
                height = height,
                sizeBytes = bytes.size.toLong(),
                format = format
            )

            Pair(meta, bytes)
        }
    }

    /**
     * Creates a temporary file in cache for camera capture.
     */
    fun createCameraTempUri(context: Context): Pair<Uri, File> {
        val storageDir = File(context.cacheDir, "camera_captures").apply { mkdirs() }
        val tempFile = File.createTempFile("photo_${System.currentTimeMillis()}_", ".jpg", storageDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        return Pair(uri, tempFile)
    }

    /**
     * Saves processed bytes to device gallery/MediaStore.
     */
    fun saveImageToGallery(
        context: Context,
        bytes: ByteArray,
        mimeType: String,
        format: ImageFormat,
        suggestedName: String = "padded_image"
    ): Result<Uri> {
        return runCatching {
            val extension = format.extension
            val timestamp = System.currentTimeMillis()
            val finalName = "${suggestedName}_$timestamp.$extension"
            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, finalName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ImagePadder")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val uri = resolver.insert(collection, contentValues)
                ?: throw IllegalStateException("Failed to insert MediaStore record.")

            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(bytes)
                outputStream.flush()
            } ?: throw IllegalStateException("Failed to open output stream for MediaStore URI.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            uri
        }
    }

    /**
     * Writes processed bytes to cache and creates a Share Intent with FileProvider.
     */
    fun createShareIntent(
        context: Context,
        bytes: ByteArray,
        mimeType: String,
        fileName: String
    ): Result<Intent> {
        return runCatching {
            val cacheDir = File(context.cacheDir, "shared_exports").apply { mkdirs() }
            val exportFile = File(cacheDir, fileName)
            FileOutputStream(exportFile).use { fos ->
                fos.write(bytes)
                fos.flush()
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )

            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }

    /**
     * Generates standard sample images matching the UI designs.
     */
    fun createSampleImage(format: ImageFormat, width: Int = 1200, height: Int = 800): Pair<ImageMetadata, ByteArray> {
        return when (format) {
            ImageFormat.JPEG -> createArtCubesSample(width, height)
            ImageFormat.PNG -> createMotherboardSample(width, height)
            ImageFormat.WEBP -> createCosmicSpheresSample(width, height)
            else -> createArtCubesSample(width, height)
        }
    }

    fun createArtCubesBitmap(width: Int = 1200, height: Int = 800): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Minimalist exhibition hall background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.rgb(200, 202, 208),
                Color.rgb(40, 42, 48),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Floor perspective line
        val floorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(28, 30, 34)
        }
        canvas.drawRect(0f, height * 0.72f, width.toFloat(), height.toFloat(), floorPaint)

        // Hanging wires
        val wirePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 180, 180, 190)
            strokeWidth = 2f
        }
        for (x in 150..width - 150 step 90) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height * 0.65f, wirePaint)
        }

        // Hanging 3D cubes / polyhedrons
        val cubePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cubePositions = listOf(
            Triple(width * 0.22f, height * 0.38f, 75f),
            Triple(width * 0.38f, height * 0.28f, 95f),
            Triple(width * 0.50f, height * 0.44f, 110f),
            Triple(width * 0.65f, height * 0.32f, 85f),
            Triple(width * 0.78f, height * 0.42f, 70f),
            Triple(width * 0.32f, height * 0.52f, 65f),
            Triple(width * 0.60f, height * 0.55f, 60f)
        )

        for ((cx, cy, sz) in cubePositions) {
            // Isometric cube faces
            val pathTop = android.graphics.Path().apply {
                moveTo(cx, cy - sz)
                lineTo(cx + sz * 0.86f, cy - sz * 0.5f)
                lineTo(cx, cy)
                lineTo(cx - sz * 0.86f, cy - sz * 0.5f)
                close()
            }
            cubePaint.color = Color.rgb(180, 40, 45) // accent red
            canvas.drawPath(pathTop, cubePaint)

            val pathLeft = android.graphics.Path().apply {
                moveTo(cx - sz * 0.86f, cy - sz * 0.5f)
                lineTo(cx, cy)
                lineTo(cx, cy + sz)
                lineTo(cx - sz * 0.86f, cy + sz * 0.5f)
                close()
            }
            cubePaint.color = Color.rgb(24, 25, 28)
            canvas.drawPath(pathLeft, cubePaint)

            val pathRight = android.graphics.Path().apply {
                moveTo(cx, cy)
                lineTo(cx + sz * 0.86f, cy - sz * 0.5f)
                lineTo(cx + sz * 0.86f, cy + sz * 0.5f)
                lineTo(cx, cy + sz)
                close()
            }
            cubePaint.color = Color.rgb(60, 62, 68)
            canvas.drawPath(pathRight, cubePaint)
        }
        return bitmap
    }

    fun createArtCubesSample(width: Int = 1200, height: Int = 800): Pair<ImageMetadata, ByteArray> {
        val bitmap = createArtCubesBitmap(width, height)
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        val bytes = out.toByteArray()
        val meta = ImageMetadata(
            fileName = "gallery_cubes_art.jpg",
            mimeType = "image/jpeg",
            width = width,
            height = height,
            sizeBytes = bytes.size.toLong(),
            format = ImageFormat.JPEG
        )
        return Pair(meta, bytes)
    }

    fun createMotherboardBitmap(width: Int = 1200, height: Int = 800): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dark PCB board background
        val pcbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                Color.rgb(10, 18, 30),
                Color.rgb(18, 38, 58),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), pcbPaint)

        // Circuit board traces
        val tracePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(160, 48, 120, 170)
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(80, 180, 230)
            style = Paint.Style.FILL
        }

        for (i in 0..16) {
            val startY = 80f + i * 40f
            val path = android.graphics.Path().apply {
                moveTo(60f, startY)
                lineTo(280f + (i % 4) * 50f, startY)
                lineTo(360f + (i % 4) * 50f, startY + 60f)
                lineTo(width - 80f, startY + 60f)
            }
            canvas.drawPath(path, tracePaint)
            canvas.drawCircle(60f, startY, 5f, dotPaint)
            canvas.drawCircle(width - 80f, startY + 60f, 5f, dotPaint)
        }

        // Central CPU chip
        val cpuPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(12, 22, 36)
            style = Paint.Style.FILL
        }
        val cpuBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(70, 140, 190)
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }
        val cx = width * 0.58f
        val cy = height * 0.46f
        val cpuSize = 220f
        canvas.drawRoundRect(cx - cpuSize, cy - cpuSize, cx + cpuSize, cy + cpuSize, 24f, 24f, cpuPaint)
        canvas.drawRoundRect(cx - cpuSize, cy - cpuSize, cx + cpuSize, cy + cpuSize, 24f, 24f, cpuBorder)

        // Chip core
        val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(25, 50, 75)
        }
        canvas.drawRoundRect(cx - cpuSize * 0.65f, cy - cpuSize * 0.65f, cx + cpuSize * 0.65f, cy + cpuSize * 0.65f, 16f, 16f, corePaint)
        return bitmap
    }

    fun createMotherboardSample(width: Int = 1200, height: Int = 800): Pair<ImageMetadata, ByteArray> {
        val bitmap = createMotherboardBitmap(width, height)
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        val bytes = out.toByteArray()
        val meta = ImageMetadata(
            fileName = "circuit_board_chip.png",
            mimeType = "image/png",
            width = width,
            height = height,
            sizeBytes = bytes.size.toLong(),
            format = ImageFormat.PNG
        )
        return Pair(meta, bytes)
    }

    fun createCosmicSpheresBitmap(width: Int = 1200, height: Int = 800): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Deep cosmos space
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.5f, height * 0.5f, width * 0.7f,
                Color.rgb(20, 24, 42),
                Color.rgb(5, 6, 12),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Stars
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        for (i in 0..120) {
            val sx = (Math.random() * width).toFloat()
            val sy = (Math.random() * height).toFloat()
            val sr = (Math.random() * 2f + 1f).toFloat()
            canvas.drawCircle(sx, sy, sr, starPaint)
        }

        // Cosmic glass spheres
        val spheres = listOf(
            Triple(width * 0.52f, height * 0.48f, 140f),
            Triple(width * 0.32f, height * 0.62f, 95f),
            Triple(width * 0.72f, height * 0.55f, 105f),
            Triple(width * 0.28f, height * 0.34f, 80f),
            Triple(width * 0.68f, height * 0.32f, 85f),
            Triple(width * 0.44f, height * 0.28f, 65f)
        )

        for ((sx, sy, sr) in spheres) {
            val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    sx - sr * 0.3f, sy - sr * 0.3f, sr * 1.1f,
                    Color.argb(240, 110, 160, 230),
                    Color.argb(220, 25, 20, 50),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(sx, sy, sr, orbPaint)

            // Inner galaxy swirl
            val swirlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(120, 200, 150, 255)
                strokeWidth = 6f
                style = Paint.Style.STROKE
            }
            canvas.drawCircle(sx, sy, sr * 0.6f, swirlPaint)

            // Glossy rim highlight
            val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(180, 255, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }
            canvas.drawCircle(sx, sy, sr, rimPaint)
        }
        return bitmap
    }

    fun createCosmicSpheresSample(width: Int = 1200, height: Int = 800): Pair<ImageMetadata, ByteArray> {
        val bitmap = createCosmicSpheresBitmap(width, height)
        val out = ByteArrayOutputStream()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 90, out)
        } else {
            @Suppress("DEPRECATION")
            bitmap.compress(Bitmap.CompressFormat.WEBP, 90, out)
        }
        val bytes = out.toByteArray()
        val meta = ImageMetadata(
            fileName = "cosmic_spheres.webp",
            mimeType = "image/webp",
            width = width,
            height = height,
            sizeBytes = bytes.size.toLong(),
            format = ImageFormat.WEBP
        )
        return Pair(meta, bytes)
    }

    fun createMechWatchSample(width: Int = 1200, height: Int = 750): Pair<ImageMetadata, ByteArray> {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.5f, height * 0.5f, width * 0.6f,
                Color.rgb(12, 28, 48),
                Color.rgb(3, 6, 12),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Circular watch bezel
        val cx = width * 0.5f
        val cy = height * 0.5f
        val radius = height * 0.42f

        val bezelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 36, 56)
            style = Paint.Style.STROKE
            strokeWidth = 28f
        }
        canvas.drawCircle(cx, cy, radius, bezelPaint)

        // Gears inside
        val gearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(180, 140, 70) // Brass gold gear
            style = Paint.Style.STROKE
            strokeWidth = 14f
        }
        canvas.drawCircle(cx - 70f, cy + 40f, 110f, gearPaint)

        val gear2Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(75, 120, 160) // Steel balance
            style = Paint.Style.STROKE
            strokeWidth = 10f
        }
        canvas.drawCircle(cx + 80f, cy - 30f, 130f, gear2Paint)

        val centerPin = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(210, 45, 55) // Jewel ruby
        }
        canvas.drawCircle(cx, cy, 14f, centerPin)
        canvas.drawCircle(cx - 70f, cy + 40f, 10f, centerPin)

        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        val bytes = out.toByteArray()
        val meta = ImageMetadata(
            fileName = "Mech_Watch_01.png",
            mimeType = "image/png",
            width = width,
            height = height,
            sizeBytes = bytes.size.toLong(),
            format = ImageFormat.PNG
        )
        return Pair(meta, bytes)
    }

    fun createServerRoomSample(width: Int = 1200, height: Int = 750): Pair<ImageMetadata, ByteArray> {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(8, 14, 22)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Vanishing perspective corridor
        val corridorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                width * 0.5f, height.toFloat(), width * 0.5f, height * 0.45f,
                Color.rgb(30, 80, 120),
                Color.rgb(8, 14, 22),
                Shader.TileMode.CLAMP
            )
        }
        val floorPath = android.graphics.Path().apply {
            moveTo(width * 0.35f, height.toFloat())
            lineTo(width * 0.65f, height.toFloat())
            lineTo(width * 0.52f, height * 0.45f)
            lineTo(width * 0.48f, height * 0.45f)
            close()
        }
        canvas.drawPath(floorPath, corridorPaint)

        // Server rack outlines
        val rackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(18, 26, 38)
            style = Paint.Style.FILL
        }
        canvas.drawRect(80f, 80f, width * 0.35f, height.toFloat(), rackPaint)
        canvas.drawRect(width * 0.65f, 80f, width - 80f, height.toFloat(), rackPaint)

        // Glowing cyan LED strips
        val ledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(60, 210, 245)
            strokeWidth = 4f
        }
        for (y in 120..height - 80 step 30) {
            canvas.drawLine(width * 0.34f, y.toFloat(), width * 0.35f, y.toFloat(), ledPaint)
            canvas.drawLine(width * 0.65f, y.toFloat(), width * 0.66f, y.toFloat(), ledPaint)
        }

        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        val bytes = out.toByteArray()
        val meta = ImageMetadata(
            fileName = "Server_Room_Deep.jpg",
            mimeType = "image/jpeg",
            width = width,
            height = height,
            sizeBytes = bytes.size.toLong(),
            format = ImageFormat.JPEG
        )
        return Pair(meta, bytes)
    }

    fun createAbstractGeoSample(width: Int = 1200, height: Int = 750): Pair<ImageMetadata, ByteArray> {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(14, 16, 22)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Origami polygonal shards
        val shardColors = listOf(
            Color.rgb(24, 28, 40),
            Color.rgb(36, 42, 60),
            Color.rgb(18, 22, 32),
            Color.rgb(45, 55, 78)
        )
        val shardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(70, 110, 180)
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }

        val points = listOf(
            listOf(Offset(width * 0.15f, height * 0.55f), Offset(width * 0.35f, height * 0.35f), Offset(width * 0.45f, height * 0.65f)),
            listOf(Offset(width * 0.35f, height * 0.35f), Offset(width * 0.60f, height * 0.30f), Offset(width * 0.45f, height * 0.65f)),
            listOf(Offset(width * 0.60f, height * 0.30f), Offset(width * 0.80f, height * 0.45f), Offset(width * 0.65f, height * 0.70f)),
            listOf(Offset(width * 0.45f, height * 0.65f), Offset(width * 0.65f, height * 0.70f), Offset(width * 0.55f, height * 0.85f))
        )

        for (i in points.indices) {
            val tri = points[i]
            val path = android.graphics.Path().apply {
                moveTo(tri[0].x, tri[0].y)
                lineTo(tri[1].x, tri[1].y)
                lineTo(tri[2].x, tri[2].y)
                close()
            }
            shardPaint.color = shardColors[i % shardColors.size]
            canvas.drawPath(path, shardPaint)
            canvas.drawPath(path, edgePaint)
        }

        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        val bytes = out.toByteArray()
        val meta = ImageMetadata(
            fileName = "Abstract_Geo_Dark.png",
            mimeType = "image/png",
            width = width,
            height = height,
            sizeBytes = bytes.size.toLong(),
            format = ImageFormat.PNG
        )
        return Pair(meta, bytes)
    }

    fun createSciFiHudSample(width: Int = 1200, height: Int = 750): Pair<ImageMetadata, ByteArray> {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.5f, height * 0.5f, width * 0.7f,
                Color.rgb(10, 24, 38),
                Color.rgb(3, 5, 8),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Glass HUD pane
        val hudBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(160, 15, 28, 45)
        }
        val hudBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(60, 180, 210)
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val hudMarginX = 140f
        val hudMarginY = 100f
        canvas.drawRoundRect(hudMarginX, hudMarginY, width - hudMarginX, height - hudMarginY, 20f, 20f, hudBg)
        canvas.drawRoundRect(hudMarginX, hudMarginY, width - hudMarginX, height - hudMarginY, 20f, 20f, hudBorder)

        // Title text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(180, 240, 255)
            textSize = 36f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("EXPORT GALLERY", width * 0.5f, hudMarginY + 60f, textPaint)

        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        val bytes = out.toByteArray()
        val meta = ImageMetadata(
            fileName = "Telemetry_HUD_04.png",
            mimeType = "image/png",
            width = width,
            height = height,
            sizeBytes = bytes.size.toLong(),
            format = ImageFormat.PNG
        )
        return Pair(meta, bytes)
    }

    private class Offset(val x: Float, val y: Float)


    private fun readAllBytes(inputStream: InputStream): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var len: Int
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }
}
