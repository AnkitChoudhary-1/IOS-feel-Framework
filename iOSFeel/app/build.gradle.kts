plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "dev.iosfeel.lab"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.iosfeel.lab"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
}

dependencies {
    // Internal modules
    implementation(project(":iosfeel-core"))
    implementation(project(":iosfeel-motion"))
    implementation(project(":iosfeel-haptics"))
    implementation(project(":iosfeel-gesture"))
    implementation(project(":iosfeel-navigation"))
    implementation(project(":iosfeel-scroll"))
    implementation(project(":iosfeel-sheet"))
    implementation(project(":iosfeel-material"))
    implementation(project(":iosfeel-components"))

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
    implementation(libs.navigation.compose)

    // Debug tooling
    debugImplementation(libs.compose.ui.tooling)

    // Testing
    testImplementation(libs.junit)
}
