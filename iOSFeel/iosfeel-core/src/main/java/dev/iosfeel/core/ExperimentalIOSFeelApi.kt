package dev.iosfeel.core

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This is an experimental iOSFeel API and may change or be removed in future releases."
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.TYPEALIAS
)
annotation class ExperimentalIOSFeelApi
