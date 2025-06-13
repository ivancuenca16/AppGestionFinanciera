plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.cuenca.appgestionfinanciera"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cuenca.appgestionfinanciera"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Material Components (necesario para TextInputLayout, MaterialButton, etc.)
    implementation("com.google.android.material:material:1.6.1")

    // CardView (elevar contenedores)
    implementation("androidx.cardview:cardview:1.0.0")

    // RecyclerView, ConstraintLayout,
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database:20.1.0")

    // Material Components
    implementation("com.google.android.material:material:1.6.1")

    // RecyclerView y ConstraintLayout
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")


}
