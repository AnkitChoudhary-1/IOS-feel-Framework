package dev.iosfeel.physics

/**
 * Annotation for experimental iOSFeel V2 APIs.
 *
 * APIs marked with this annotation are part of the iOSFeel V2 physics and interaction
 * engine, and may evolve as components and physical tuning stabilize.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This is an experimental iOSFeel V2 API. It may change or be refined in future releases."
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS
)
annotation class ExperimentalIOSFeelV2Api
