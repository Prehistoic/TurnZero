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

    sourceSets {
        getByName("main") {
            assets.srcDirs(layout.buildDirectory.dir("generated/assets"))
        }
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
    description = "Generates licenses.json from resolved dependencies"

    val outputDir = layout.buildDirectory.dir("generated/assets/licenses")
    val outputFile = outputDir.map { it.file("licenses.json") }

    inputs.files(configurations.runtimeClasspath)
    outputs.dir(outputDir)

    doLast {
        val deps = mutableListOf<Map<String, String>>()

        configurations.runtimeClasspath.get().resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
            try {
                val module = artifact.moduleVersion.id
                val pomFile = artifact.file.resolveSibling(
                    "${module.name}-${module.version}.pom"
                )
                val licenses = mutableListOf<String>()
                if (pomFile.exists()) {
                    val xml = groovy.xml.XmlSlurper().parse(pomFile)
                    xml.licenses.license.each { lic ->
                        licenses.add(lic.name.text().trim())
                    }
                }
                if (licenses.isEmpty()) {
                    licenses.add("Unknown")
                }
                deps.add(mapOf(
                    "name" to module.name,
                    "group" to module.group,
                    "version" to module.version,
                    "licenses" to licenses.joinToString(", ")
                ))
            } catch (_: Exception) {
            }
        }

        val json = groovy.json.JsonOutput.toJson(deps.distinctBy { "${it["group"]}:${it["name"]}" }
            .sortedBy { it["name"].toString().lowercase() })

        outputFile.get().asFile.parentFile.mkdirs()
        outputFile.get().asFile.writeText(json)
    }
}

tasks.named("preBuild") {
    dependsOn(generateLicensesJson)
}
