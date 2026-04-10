import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

val keystoreProperties = Properties().apply {
      val file = rootProject.file("keystore.properties")
      if (file.exists()) {
          file.inputStream().use(::load)
      }
  }

android {
      namespace = "com.example.expensetracker"
      compileSdk = 34

      defaultConfig {
          applicationId = "com.example.expensetracker"
          minSdk = 26
          targetSdk = 34
          versionCode = 1
          versionName = "1.0"

          testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
          vectorDrawables {
              useSupportLibrary = true
          }
      }

      signingConfigs {
          create("release") {
              if (keystoreProperties.isNotEmpty()) {
                  storeFile = file(keystoreProperties["storeFile"] as String)
                  storePassword = keystoreProperties["storePassword"] as String
                  keyAlias = keystoreProperties["keyAlias"] as String
                  keyPassword = keystoreProperties["keyPassword"] as String
              }
          }
      }

      buildTypes {
          release {
              signingConfig = signingConfigs.getByName("release")
              isMinifyEnabled = false
              proguardFiles(
                  getDefaultProguardFile("proguard-android-optimize.txt"),
                  "proguard-rules.pro",
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
          buildConfig = true
      }

      composeOptions {
          kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
      }

      packaging {
          resources {
              excludes += "/META-INF/{AL2.0,LGPL2.1}"
          }
      }
  }

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.google.material)
    implementation(libs.hilt.android)

    kapt(libs.androidx.room.compiler)
    kapt(libs.hilt.compiler)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
