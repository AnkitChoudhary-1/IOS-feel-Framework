package dev.iosfeel.motion.morph

import org.junit.Assert.assertEquals
import org.junit.Test

class IOSMorphContentTest {

    @Test
    fun `interval progress maps sub interval correctly`() {
        // Interval: 0.2 to 0.6
        assertEquals(0f, intervalProgress(0.1f, 0.2f, 0.6f), 0.001f)
        assertEquals(0f, intervalProgress(0.2f, 0.2f, 0.6f), 0.001f)
        assertEquals(0.5f, intervalProgress(0.4f, 0.2f, 0.6f), 0.001f)
        assertEquals(1f, intervalProgress(0.6f, 0.2f, 0.6f), 0.001f)
        assertEquals(1f, intervalProgress(0.9f, 0.2f, 0.6f), 0.001f)
    }
}
