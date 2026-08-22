package dev.iosfeel.scroll

interface IOSFlingObserver {
    fun onFlingStarted(velocity: Float)
    fun onFlingVelocityChanged(velocity: Float)
    fun onFlingEnded()
}
