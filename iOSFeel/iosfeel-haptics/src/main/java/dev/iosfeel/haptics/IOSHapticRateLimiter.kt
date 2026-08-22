package dev.iosfeel.haptics

import android.os.SystemClock

internal class IOSHapticRateLimiter(
    private val minimumIntervalMs: Long,
    private val clock: () -> Long = {
        SystemClock.elapsedRealtime()
    }
) {

    private var lastEventTime = Long.MIN_VALUE
    private var lastEventKey: Any? = null

    fun shouldPerform(
        key: Any
    ): Boolean {

        val now = clock()

        if (
            key == lastEventKey &&
            now - lastEventTime < minimumIntervalMs
        ) {
            return false
        }

        lastEventTime = now
        lastEventKey = key

        return true
    }

    fun reset() {
        lastEventTime = Long.MIN_VALUE
        lastEventKey = null
    }
}
