package dev.iosfeel.material

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSMaterialConfigTest {

    @Test
    fun defaultConfigValues() {
        val config = IOSMaterialConfig()
        assertEquals(IOSMaterialStyle.Regular, config.style)
        assertEquals(20.dp, config.cornerRadius)
        assertEquals(0.5.dp, config.borderStroke)
        assertEquals(null, config.tint)
        assertEquals(null, config.borderColor)
        assertTrue(config.enabled)
    }

    @Test
    fun customConfigOverrides() {
        val config = IOSMaterialConfig(
            style = IOSMaterialStyle.UltraThin,
            cornerRadius = 12.dp,
            borderStroke = 1.dp
        )
        assertEquals(IOSMaterialStyle.UltraThin, config.style)
        assertEquals(12.dp, config.cornerRadius)
        assertEquals(1.dp, config.borderStroke)
    }
}
