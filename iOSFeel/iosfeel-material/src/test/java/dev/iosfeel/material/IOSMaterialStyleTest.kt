package dev.iosfeel.material

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSMaterialStyleTest {

    @Test
    fun resolveAllMaterialStyles() {
        val ultraThin = resolveIOSMaterial(IOSMaterialStyle.UltraThin)
        assertEquals(10.dp, ultraThin.blurRadius)
        assertEquals(0.55f, ultraThin.tintAlpha, 0.001f)

        val thin = resolveIOSMaterial(IOSMaterialStyle.Thin)
        assertEquals(16.dp, thin.blurRadius)
        assertEquals(0.68f, thin.tintAlpha, 0.001f)

        val regular = resolveIOSMaterial(IOSMaterialStyle.Regular)
        assertEquals(24.dp, regular.blurRadius)
        assertEquals(0.78f, regular.tintAlpha, 0.001f)

        val thick = resolveIOSMaterial(IOSMaterialStyle.Thick)
        assertEquals(32.dp, thick.blurRadius)
        assertEquals(0.88f, thick.tintAlpha, 0.001f)
    }

    @Test
    fun blurRadiusIncreasesWithDensity() {
        val ultraThin = resolveIOSMaterial(IOSMaterialStyle.UltraThin)
        val thick = resolveIOSMaterial(IOSMaterialStyle.Thick)

        assertTrue(thick.blurRadius > ultraThin.blurRadius)
        assertTrue(thick.tintAlpha > ultraThin.tintAlpha)
    }
}
