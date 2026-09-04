package com.umair.exactpic

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.umair.exactpic.engine.ImagePadderEngine
import com.umair.exactpic.model.ImageFormat
import com.umair.exactpic.util.ImageUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ExactPic", appName)
  }

  @Test
  fun `test JPEG binary padding with COM marker`() {
    val (_, bytes) = ImageUtils.createSampleImage(ImageFormat.JPEG, 200, 200)
    val targetBytes = bytes.size.toLong() + 10240L // +10KB
    val padded = ImagePadderEngine.padJpegComMarker(bytes, targetBytes)
    assertEquals(targetBytes, padded.size.toLong())
    // Check SOI
    assertEquals(0xFF.toByte(), padded[0])
    assertEquals(0xD8.toByte(), padded[1])
    // Check COM marker right after SOI
    assertEquals(0xFF.toByte(), padded[2])
    assertEquals(0xFE.toByte(), padded[3])
  }

  @Test
  fun `test PNG binary padding with tEXt chunk`() {
    val (_, bytes) = ImageUtils.createSampleImage(ImageFormat.PNG, 200, 200)
    val targetBytes = bytes.size.toLong() + 20480L // +20KB
    val padded = ImagePadderEngine.padPngTextChunk(bytes, targetBytes)
    assertEquals(targetBytes, padded.size.toLong())
    // PNG signature check
    assertEquals(0x89.toByte(), padded[0])
    assertEquals('P'.code.toByte(), padded[1])
    assertEquals('N'.code.toByte(), padded[2])
    assertEquals('G'.code.toByte(), padded[3])
  }

  @Test
  fun `test clearLoadedImage resets state to empty image selection screen`() {
    val viewModel = com.umair.exactpic.viewmodel.ImagePadderViewModel()
    viewModel.loadSample(ImageFormat.JPEG)
    // Clear and verify
    viewModel.clearLoadedImage()
    val state = viewModel.uiState.value
    org.junit.Assert.assertNull(state.originalBytes)
    org.junit.Assert.assertNull(state.originalMetadata)
    org.junit.Assert.assertNull(state.processedBytes)
    org.junit.Assert.assertNull(state.processedMetadata)
  }
}

