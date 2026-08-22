pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "iOSFeel"
include(":app")
include(":iosfeel-core")
include(":iosfeel-motion")
include(":iosfeel-haptics")
include(":iosfeel-gesture")
include(":iosfeel-navigation")
include(":iosfeel-scroll")
include(":iosfeel-sheet")
include(":iosfeel-material")
include(":iosfeel-components")
