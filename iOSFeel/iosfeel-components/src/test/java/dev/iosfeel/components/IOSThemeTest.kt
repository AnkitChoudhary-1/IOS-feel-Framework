package dev.iosfeel.components

import dev.iosfeel.components.theme.IOSFeelDarkColors
import dev.iosfeel.components.theme.IOSFeelLightColors
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class IOSThemeTest {

    @Test
    fun themeColorsDefined() {
        assertNotNull(IOSFeelLightColors.background)
        assertNotNull(IOSFeelDarkColors.background)
        assertNotNull(IOSFeelLightColors.accent)
        assertNotNull(IOSFeelDarkColors.accent)

        assertNotEquals(IOSFeelLightColors.background, IOSFeelDarkColors.background)
    }
}
