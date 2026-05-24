import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.hilt)
  kotlin("kapt")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val appwriteProjectId = localProperties.getProperty("APPWRITE_PROJECT_ID") ?: ""

android {
    namespace = "com.behavioralfirewall"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.behavioralfirewall"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        
        buildConfigField("String", "APPWRITE_PROJECT_ID", "\"${appwriteProjectId}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

kapt {
    correctErrorTypes = true
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation("org.mockito:mockito-core:5.7.0")

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Secure Local Database (Room + SQLCipher)
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  "kapt"(libs.room.compiler)
  implementation(libs.sqlcipher)

  // Dependency Injection (Hilt)
  implementation(libs.hilt.android)
  "kapt"(libs.hilt.compiler)
  implementation(libs.hilt.navigation.compose)

  // Appwrite & WorkManager
  implementation(libs.appwrite.sdk) {
      exclude(group = "com.squareup.okhttp3", module = "okhttp-bom")
  }
  implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
  implementation(libs.work.runtime)

  // Hilt WorkManager Integration
  implementation("androidx.hilt:hilt-work:1.2.0")
  "kapt"("androidx.hilt:hilt-compiler:1.2.0")
}
