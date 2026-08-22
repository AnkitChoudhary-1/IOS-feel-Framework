package dev.iosfeel.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class IOSNavigationTransitionTest {

    @Test
    fun backTransform_atZeroProgress() {
        val transform = calculateIOSBackTransform(0f)
        assertEquals(0f, transform.currentTranslationFraction, 0.001f)
        assertEquals(-0.25f, transform.previousTranslationFraction, 0.001f)
        assertEquals(1f, transform.shadowAlpha, 0.001f)
    }

    @Test
    fun backTransform_atHalfProgress() {
        val transform = calculateIOSBackTransform(0.5f)
        assertEquals(0.5f, transform.currentTranslationFraction, 0.001f)
        assertEquals(-0.125f, transform.previousTranslationFraction, 0.001f)
        assertEquals(0.5f, transform.shadowAlpha, 0.001f)
    }

    @Test
    fun backTransform_atCompletion() {
        val transform = calculateIOSBackTransform(1f)
        assertEquals(1f, transform.currentTranslationFraction, 0.001f)
        assertEquals(0f, transform.previousTranslationFraction, 0.001f)
        assertEquals(0f, transform.shadowAlpha, 0.001f)
    }

    @Test
    fun pushTransform_atZeroProgress() {
        val transform = calculateIOSPushTransform(0f)
        assertEquals(1f, transform.currentTranslationFraction, 0.001f)
        assertEquals(0f, transform.previousTranslationFraction, 0.001f)
    }

    @Test
    fun pushTransform_atHalfProgress() {
        val transform = calculateIOSPushTransform(0.5f)
        assertEquals(0.5f, transform.currentTranslationFraction, 0.001f)
        assertEquals(-0.125f, transform.previousTranslationFraction, 0.001f)
    }

    @Test
    fun pushTransform_atCompletion() {
        val transform = calculateIOSPushTransform(1f)
        assertEquals(0f, transform.currentTranslationFraction, 0.001f)
        assertEquals(-0.25f, transform.previousTranslationFraction, 0.001f)
    }

    @Test
    fun velocityIsNormalizedByDistance() {
        assertEquals(
            2f,
            normalizeGestureVelocity(
                velocityPxPerSecond = 2000f,
                distancePx = 1000f
            ),
            0.001f
        )
    }

    @Test
    fun regrabDoesNotJumpBackToZero() {
        assertEquals(
            0.32f,
            mapRegrabProgress(
                startProgress = 0.32f,
                gestureProgress = 0f
            ),
            0.001f
        )
    }

    @Test
    fun halfRemainingDragMapsCorrectly() {
        assertEquals(
            0.66f,
            mapRegrabProgress(
                startProgress = 0.32f,
                gestureProgress = 0.5f
            ),
            0.001f
        )
    }
}
