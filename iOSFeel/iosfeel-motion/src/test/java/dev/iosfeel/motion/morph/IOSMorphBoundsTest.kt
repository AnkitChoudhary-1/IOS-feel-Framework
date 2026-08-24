package dev.iosfeel.motion.morph

import org.junit.Assert.assertEquals
import org.junit.Test

class IOSMorphBoundsTest {

    @Test
    fun `bounds calculate correct width height and center`() {
        val bounds = IOSMorphBounds(left = 20f, top = 50f, right = 120f, bottom = 150f)
        assertEquals(100f, bounds.width, 0.001f)
        assertEquals(100f, bounds.height, 0.001f)
        assertEquals(70f, bounds.center.x, 0.001f)
        assertEquals(100f, bounds.center.y, 0.001f)
    }

    @Test
    fun `lerp smoothly interpolates start and end bounds`() {
        val start = IOSMorphBounds(left = 0f, top = 0f, right = 100f, bottom = 100f)
        val end = IOSMorphBounds(left = 100f, top = 200f, right = 300f, bottom = 400f)

        val mid = IOSMorphBounds.lerp(start, end, 0.5f)
        assertEquals(50f, mid.left, 0.001f)
        assertEquals(100f, mid.top, 0.001f)
        assertEquals(200f, mid.right, 0.001f)
        assertEquals(250f, mid.bottom, 0.001f)
        assertEquals(150f, mid.width, 0.001f)
        assertEquals(150f, mid.height, 0.001f)
    }
}
