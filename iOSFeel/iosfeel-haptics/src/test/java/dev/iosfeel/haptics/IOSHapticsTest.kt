package dev.iosfeel.haptics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSHapticsTest {

    @Test
    fun impactEnum_containsAllStrengths() {
        val impacts = IOSImpact.values()
        assertEquals(3, impacts.size)
        assertTrue(impacts.contains(IOSImpact.Light))
        assertTrue(impacts.contains(IOSImpact.Medium))
        assertTrue(impacts.contains(IOSImpact.Heavy))
    }

    @Test
    fun notificationEnum_containsAllTypes() {
        val notifications = IOSNotification.values()
        assertEquals(3, notifications.size)
        assertTrue(notifications.contains(IOSNotification.Success))
        assertTrue(notifications.contains(IOSNotification.Warning))
        assertTrue(notifications.contains(IOSNotification.Error))
    }

    @Test
    fun hapticEvents_createCorrectly() {
        val selection = IOSHapticEvent.Selection
        val gestureStart = IOSHapticEvent.GestureStart
        val gestureEnd = IOSHapticEvent.GestureEnd
        val thresholdActivated = IOSHapticEvent.ThresholdActivated
        val thresholdDeactivated = IOSHapticEvent.ThresholdDeactivated
        val impact = IOSHapticEvent.Impact(IOSImpact.Light)
        val notification = IOSHapticEvent.Notification(IOSNotification.Success)

        assertEquals(IOSImpact.Light, impact.strength)
        assertEquals(IOSNotification.Success, notification.type)
        assertEquals(IOSHapticEvent.Selection, selection)
        assertEquals(IOSHapticEvent.GestureStart, gestureStart)
        assertEquals(IOSHapticEvent.GestureEnd, gestureEnd)
        assertEquals(IOSHapticEvent.ThresholdActivated, thresholdActivated)
        assertEquals(IOSHapticEvent.ThresholdDeactivated, thresholdDeactivated)
    }

    @Test
    fun hapticCapabilities_defaultState() {
        val caps = IOSHapticCapabilities(
            hasVibrator = true,
            supportsTick = true,
            supportsClick = true,
            supportsHeavyClick = true,
            supportsPrimitiveClick = true,
            supportsPrimitiveTick = true,
            supportsPrimitiveLowTick = false
        )

        assertTrue(caps.hasVibrator)
        assertTrue(caps.supportsTick)
        assertTrue(caps.supportsClick)
        assertTrue(caps.supportsHeavyClick)
        assertTrue(caps.supportsPrimitiveClick)
        assertTrue(caps.supportsPrimitiveTick)
    }

    @Test
    fun hapticPolicy_defaultsAreSensible() {
        val policy = IOSHapticPolicy()
        assertTrue(policy.enabled)
        assertEquals(35L, policy.minimumIntervalMs)
        assertTrue(policy.preferSystemFeedback)
        assertTrue(policy.allowRichEffects)
    }
}
