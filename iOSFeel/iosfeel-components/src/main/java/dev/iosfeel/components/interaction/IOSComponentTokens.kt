package dev.iosfeel.components.interaction

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings

/**
 * Centralized semantic motion tokens for iOSFeel components.
 */
@ExperimentalIOSFeelV2Api
object IOSComponentMotion {
    val ButtonPress: IOSSpringSpec = IOSSprings.Press
    val Toggle: IOSSpringSpec = IOSSprings.Selection
    val SliderRelease: IOSSpringSpec = IOSSprings.Selection
    val SegmentedSelection: IOSSpringSpec = IOSSprings.Selection
    val FloatingTabHold: IOSSpringSpec = IOSSprings.Snappy
    val FloatingTabRelease: IOSSpringSpec = IOSSprings.Responsive
    val ListRowPress: IOSSpringSpec = IOSSprings.Smooth
}

/**
 * Centralized semantic shape tokens for iOSFeel components.
 */
object IOSComponentShapes {
    val Control = RoundedCornerShape(12.dp)
    val Surface = RoundedCornerShape(18.dp)
    val FloatingBar = RoundedCornerShape(50)
    val Pill = RoundedCornerShape(50)
    val ListRow = RoundedCornerShape(12.dp)
    val Badge = RoundedCornerShape(50)
}
