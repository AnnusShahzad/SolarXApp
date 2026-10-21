import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// AGP 9 compiles Kotlin itself, so only the Compose compiler plugin is applied here.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "io.github.annusshahzad.solarx"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.annusshahzad.solarx"
        minSdk = 29
        targetSdk = 36
        // The release workflow passes -PversionName=<tag>; the version code follows from it (1.2.3 -> 10203).
        val version = (project.findProperty("versionName") as String?) ?: "1.0.0"
        val (major, minor, patch) = Regex("""(\d+)\.(\d+)\.(\d+)""").matchEntire(version)
            ?.destructured?.toList()?.map(String::toInt)
            ?.takeIf { (_, minor, patch) -> minor < 100 && patch < 100 }
            ?: throw GradleException("versionName must look like 1.2.3 with minor and patch below 100, got '$version'")
        versionName = version
        versionCode = major * 10_000 + minor * 100 + patch

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        // Release builds are signed with the keystore from the environment when it is provided
        // (see .github/workflows/release.yml), and with the debug key otherwise so that a
        // locally built release APK can still be installed.
        create("release") {
            val keystore = System.getenv("SIGNING_KEYSTORE_PATH")
            if (keystore != null) {
                storeFile = file(keystore)
                storePassword = System.getenv("SIGNING_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (System.getenv("SIGNING_KEYSTORE_PATH") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.okhttp)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    // android.jar only ships stubs of org.json, so unit tests use the real library.
    testImplementation(libs.json)
}
