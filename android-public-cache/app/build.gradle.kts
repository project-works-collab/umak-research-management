plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "ph.edu.umak.crm.app"
    compileSdk = 35
    defaultConfig { applicationId = "ph.edu.umak.crm.app"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(project(":"))
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.work:work-runtime-ktx:2.10.1")
}
