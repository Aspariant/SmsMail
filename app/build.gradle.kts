plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "app.smsmail"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.smsmail"
        minSdk = 29
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"
    }

    buildTypes {
        release {
            // Minification is OFF on purpose: the app is tiny and R8 + JavaMail
            // is the most common source of release-only breakage.
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // android-mail and android-activation both ship the same license files.
    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE", "META-INF/LICENSE.txt", "META-INF/LICENSE.md",
                "META-INF/NOTICE", "META-INF/NOTICE.txt", "META-INF/NOTICE.md",
                "META-INF/DEPENDENCIES", "META-INF/INDEX.LIST"
            )
        }
    }
}

dependencies {
    // If Gradle sync cannot resolve a version, bump it.
    implementation("androidx.work:work-runtime:2.9.1")
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
    // QR code in Settings, so the iPhone camera can open the setup page.
    implementation("com.google.zxing:core:3.5.3")

    testImplementation("junit:junit:4.13.2")
}
