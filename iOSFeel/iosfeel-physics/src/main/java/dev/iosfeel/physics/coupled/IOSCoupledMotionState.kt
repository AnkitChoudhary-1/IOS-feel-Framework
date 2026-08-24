package dev.iosfeel.physics.coupled

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.IOSPhysicsState
import dev.iosfeel.physics.debug.IOSPhysicsEnvironment
import dev.iosfeel.physics.interruption.IOSMotionOwner
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings
import dev.iosfeel.physics.spring.animateSpring
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Specification for coupled physical bodies where motion of a primary body induces
 * a reactive motion in a secondary body (e.g. lifted tab circle induces downward navbar compression).
 *
 * @property primarySpring The spring spec applied to the primary body.
 * @property reactionSpring The spring spec applied to the reacting secondary body.
 * @property reactionStrength Multiplier mapping primary displacement to reaction displacement (e.g. -0.2f).
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSCoupledMotionSpec(
    val primarySpring: IOSSpringSpec = IOSSprings.Selection,
    val reactionSpring: IOSSpringSpec = IOSSprings.Press,
    val reactionStrength: Float = -0.20f
) {
    init {
        require(!reactionStrength.isNaN()) { "reactionStrength cannot be NaN" }
    }
}

/**
 * Holds coupled physical state for a primary body and a reacting secondary body.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSCoupledMotionState(
    initialPrimary: Float = 0f,
    initialReaction: Float = 0f
) {
    /**
     * Primary body physical state.
     */
    val primary: IOSPhysicsState = IOSPhysicsState(initialPrimary)

    /**
     * Secondary reacting body physical state.
     */
    val reaction: IOSPhysicsState = IOSPhysicsState(initialReaction)

    /**
     * Manually drives the primary body during a gesture and computes the coupled reaction displacement.
     */
    fun drivePrimary(value: Float, velocity: Float, strength: Float = -0.20f) {
        primary.update(
            value = value,
            velocity = velocity,
            target = value,
            owner = IOSMotionOwner.User
        )
        val reactionDisplacement = value * strength
        reaction.update(
            value = reactionDisplacement,
            velocity = velocity * strength,
            target = 0f,
            owner = IOSMotionOwner.Spring
        )
    }

    /**
     * Releases both bodies into active spring simulations toward their respective targets.
     */
    suspend fun release(
        primaryTarget: Float = 0f,
        primaryVelocity: Float = primary.velocity,
        spec: IOSCoupledMotionSpec = IOSCoupledMotionSpec(),
        environment: IOSPhysicsEnvironment = IOSPhysicsEnvironment()
    ) = coroutineScope {
        launch {
            animateSpring(
                state = primary,
                target = primaryTarget,
                initialVelocity = primaryVelocity,
                spec = spec.primarySpring,
                environment = environment
            )
        }
        launch {
            val reactionInitialVelocity = primaryVelocity * spec.reactionStrength
            animateSpring(
                state = reaction,
                target = 0f,
                initialVelocity = reactionInitialVelocity,
                spec = spec.reactionSpring,
                environment = environment
            )
        }
    }

    /**
     * Immediately snaps both primary and reaction states back to rest.
     */
    fun snapTo(primaryValue: Float = 0f, reactionValue: Float = 0f) {
        primary.snapTo(primaryValue)
        reaction.snapTo(reactionValue)
    }
}

/**
 * Creates and remembers an [IOSCoupledMotionState] instance.
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSCoupledMotionState(
    initialPrimary: Float = 0f,
    initialReaction: Float = 0f
): IOSCoupledMotionState {
    return remember {
        IOSCoupledMotionState(
            initialPrimary = initialPrimary,
            initialReaction = initialReaction
        )
    }
}
