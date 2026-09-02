package com.umair.exactpic

import com.umair.exactpic.model.DimensionUnit
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testPixelsConversion() {
    val unit = DimensionUnit.PIXELS
    assertEquals(1920, unit.toPixels(1920.0, 300, 1920))
    assertEquals(1920.0, unit.fromPixels(1920, 300, 1920), 0.001)
    assertEquals("1920", unit.formatValue(1920.0))
  }

  @Test
  fun testInchesConversion() {
    val unit = DimensionUnit.INCHES
    // 4 inches at 300 DPI = 1200 px
    assertEquals(1200, unit.toPixels(4.0, 300, 1200))
    assertEquals(4.0, unit.fromPixels(1200, 300, 1200), 0.001)
    assertEquals("4", unit.formatValue(4.0))
    assertEquals("4.5", unit.formatValue(4.5))
  }

  @Test
  fun testCentimetersConversion() {
    val unit = DimensionUnit.CENTIMETERS
    // 2.54 cm at 100 DPI = 1 inch at 100 DPI = 100 px
    assertEquals(100, unit.toPixels(2.54, 100, 100))
    assertEquals(2.54, unit.fromPixels(100, 100, 100), 0.01)
  }

  @Test
  fun testMillimetersConversion() {
    val unit = DimensionUnit.MILLIMETERS
    // 25.4 mm at 100 DPI = 1 inch at 100 DPI = 100 px
    assertEquals(100, unit.toPixels(25.4, 100, 100))
    assertEquals(25.4, unit.fromPixels(100, 100, 100), 0.01)
  }

  @Test
  fun testPercentConversion() {
    val unit = DimensionUnit.PERCENT
    val originalWidth = 1000
    // 50% of 1000px = 500px
    assertEquals(500, unit.toPixels(50.0, 300, originalWidth))
    assertEquals(50.0, unit.fromPixels(500, 300, originalWidth), 0.001)
  }
}
