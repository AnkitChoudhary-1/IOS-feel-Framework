package dev.iosfeel.motion.shared

import dev.iosfeel.motion.morph.IOSMorphBounds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IOSSharedElementRegistryTest {

    @Test
    fun `registers source and target bounds and resolves them`() {
        val registry = IOSSharedElementRegistry()
        val key = IOSSharedElementKey("album-artwork-1")

        val srcBounds = IOSMorphBounds(10f, 20f, 60f, 70f)
        val tgtBounds = IOSMorphBounds(30f, 100f, 330f, 400f)

        registry.registerSource(key, srcBounds)
        registry.registerTarget(key, tgtBounds)

        assertEquals(srcBounds, registry.getSourceBounds(key))
        assertEquals(tgtBounds, registry.getTargetBounds(key))

        registry.unregister(key)
        assertNull(registry.getSourceBounds(key))
        assertNull(registry.getTargetBounds(key))
    }
}
