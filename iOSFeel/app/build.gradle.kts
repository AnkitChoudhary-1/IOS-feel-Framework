plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.iosfeel.sonora"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.iosfeel.sonora"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // iOSFeel Framework Modules
    implementation(project(":iosfeel-core"))
    implementation(project(":iosfeel-motion"))
    implementation(project(":iosfeel-haptics"))
    implementation(project(":iosfeel-gesture"))
    implementation(project(":iosfeel-navigation"))
    implementation(project(":iosfeel-scroll"))
    implementation(project(":iosfeel-sheet"))
    implementation(project(":iosfeel-material"))
    implementation(project(":iosfeel-components"))
    implementation(project(":iosfeel-physics"))
    implementation(project(":iosfeel-interaction"))

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.animation)

    // AndroidX
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)

    // Media3 (Audio Playback Engine)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.common)
    implementation(libs.media3.ui)

    // Coil (Image & Artwork Loading)
    implementation(libs.coil.compose)

    // Networking & Streaming Extraction
    implementation(libs.okhttp)
    implementation(libs.newpipe.extractor)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore Preferences
    implementation(libs.datastore.preferences)

    // Debug Tooling
    debugImplementation(libs.compose.ui.tooling)

    // Testing
    testImplementation(libs.junit)
}
