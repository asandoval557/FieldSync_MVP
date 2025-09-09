plugins {
  id("com.android.application") version "8.6.0-alpha07"
  kotlin("android") version "1.9.25"
}

android {
  namespace = "com.example.fieldsync"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.example.fieldsync"
    minSdk = 24
    targetSdk = 35
    versionCode = 1
    versionName = "0.1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables.useSupportLibrary = true
  }

  buildFeatures {
    compose = true
    viewBinding = true
    buildConfig = true
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

  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.15"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  packaging {
    resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
  }
}
configurations.all {
  resolutionStrategy {
    force("androidx.lifecycle:lifecycle-runtime-compose-android:2.8.0")
    force("androidx.compose.runtime:runtime-saveable-android:1.8.0")
  }
}

dependencies {
  implementation("androidx.legacy:legacy-support-v4:1.0.0")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.3")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.3")
  implementation("androidx.fragment:fragment-ktx:1.5.6")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.13.0")
  implementation("androidx.activity:activity:1.10.1")
  implementation("androidx.constraintlayout:constraintlayout:2.2.1")

  val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
  implementation(composeBom)
  androidTestImplementation(composeBom)

  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
  implementation("androidx.activity:activity-compose:1.9.2")
  implementation("androidx.lifecycle:lifecycle-runtime-compose-android:2.8.0")

  implementation("androidx.compose.runtime:runtime-saveable-android:1.8.0")

  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3:1.3.0")

  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

