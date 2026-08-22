package dev.iosfeel.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSMotionTest {

    @Test
    fun springPresets_haveValidPhysicsParameters() {
        val presets = listOf(
            IOSMotionPreset.Snappy,
            IOSMotionPreset.Smooth,
            IOSMotionPreset.Gentle
        )

        for (preset in presets) {
            assertTrue("Stiffness must be positive", preset.stiffness > 0f)
            assertTrue("Damping ratio must be positive", preset.dampingRatio > 0f)
        }
    }

    @Test
    fun snappyPreset_isStifferThanGentle() {
        assertTrue(
            "Snappy should have higher stiffness than Gentle",
            IOSMotionPreset.Snappy.stiffness > IOSMotionPreset.Gentle.stiffness
        )
    }

    @Test
    fun motionPhases_allExist() {
        val phases = IOSMotionPhase.values()
        assertEquals(4, phases.size)
        assertTrue(phases.contains(IOSMotionPhase.Idle))
        assertTrue(phases.contains(IOSMotionPhase.Dragging))
        assertTrue(phases.contains(IOSMotionPhase.Springing))
        assertTrue(phases.contains(IOSMotionPhase.Cancelled))
    }

    @Test
    fun motionState_progressBetweenCalculatesCorrectly() {
        val state = IOSMotionState(initialPosition = 50f)
        assertEquals(0.5f, state.progressBetween(0f, 100f), 0.001f)
        assertEquals(0f, state.progressBetween(50f, 100f), 0.001f)
        assertEquals(1f, state.progressBetween(0f, 50f), 0.001f)
    }
}
