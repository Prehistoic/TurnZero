plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.turnzero"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.turnzero"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core:tools-api"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))

    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

val generateLicensesJson by tasks.registering {
    group = "licenses"
    description = "Generates licenses.json for OSS components"

    val outputDir = layout.projectDirectory.dir("src/main/assets/licenses")
    val outputFile = outputDir.file("licenses.json")

    outputs.file(outputFile)

    doLast {
        val deps = listOf(
            mapOf("name" to "Compose BOM", "group" to "androidx.compose", "version" to "2024.12.01", "licenses" to "Apache-2.0"),
            mapOf("name" to "Compose UI", "group" to "androidx.compose.ui", "version" to "2024.12.01", "licenses" to "Apache-2.0"),
            mapOf("name" to "Compose Material3", "group" to "androidx.compose.material3", "version" to "2024.12.01", "licenses" to "Apache-2.0"),
            mapOf("name" to "Material Icons Extended", "group" to "androidx.compose.material", "version" to "2024.12.01", "licenses" to "Apache-2.0"),
            mapOf("name" to "Navigation Compose", "group" to "androidx.navigation", "version" to "2.9.8", "licenses" to "Apache-2.0"),
            mapOf("name" to "Lifecycle Runtime Compose", "group" to "androidx.lifecycle", "version" to "2.10.0", "licenses" to "Apache-2.0"),
            mapOf("name" to "Lifecycle ViewModel Compose", "group" to "androidx.lifecycle", "version" to "2.10.0", "licenses" to "Apache-2.0"),
            mapOf("name" to "Activity Compose", "group" to "androidx.activity", "version" to "1.12.4", "licenses" to "Apache-2.0"),
            mapOf("name" to "Core KTX", "group" to "androidx.core", "version" to "1.13.1", "licenses" to "Apache-2.0"),
            mapOf("name" to "DataStore Preferences", "group" to "androidx.datastore", "version" to "1.1.1", "licenses" to "Apache-2.0"),
            mapOf("name" to "Kotlin Coroutines Android", "group" to "org.jetbrains.kotlinx", "version" to "1.11.0", "licenses" to "Apache-2.0"),
            mapOf("name" to "JUnit", "group" to "junit", "version" to "4.13.2", "licenses" to "EPL-1.0"),
            mapOf("name" to "MockK", "group" to "io.mockk", "version" to "1.13.12", "licenses" to "Apache-2.0"),
        )

        val json = groovy.json.JsonOutput.toJson(deps.sortedBy { it["name"].toString().lowercase() })

        outputFile.asFile.parentFile.mkdirs()
        outputFile.asFile.writeText(json)
    }
}

tasks.named("preBuild") {
    dependsOn(generateLicensesJson)
}
