import java.io.File

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
    description = "Generates licenses.json from resolved dependencies"

    val outputDir = layout.projectDirectory.dir("src/main/assets/licenses")
    val outputFile = outputDir.file("licenses.json")

    outputs.dir(outputDir)

    doLast {
        val deps = mutableListOf<Map<String, String>>()
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        val seen = mutableSetOf<String>()
        val gradleUserHome = gradle.gradleUserHomeDir
        val cacheDir = File(gradleUserHome, "caches/modules-2/files-2.1")

        val configs = listOf("debugRuntimeClasspath", "releaseRuntimeClasspath")
        for (configName in configs) {
            try {
                val config = configurations.getByName(configName)
                config.resolvedConfiguration.lenientConfiguration.allModuleDependencies.forEach { dep ->
                    val key = "${dep.moduleGroup}:${dep.moduleName}"
                    if (key in seen) return@forEach
                    seen.add(key)

                    val groupPath = dep.moduleGroup.replace('.', '/')
                    val artifactDir = File(cacheDir, "$groupPath/${dep.moduleName}/${dep.moduleVersion}")
                    if (!artifactDir.exists()) return@forEach

                    val licenses = mutableListOf<String>()
                    val pomFile = artifactDir.walkTopDown().find { it.extension == "pom" }

                    if (pomFile != null) {
                        try {
                            val doc = factory.newDocumentBuilder().parse(pomFile)
                            val licenseNodes = doc.getElementsByTagName("license")
                            for (i in 0 until licenseNodes.length) {
                                val childNodes = licenseNodes.item(i).childNodes
                                for (j in 0 until childNodes.length) {
                                    val child = childNodes.item(j)
                                    if (child.nodeName == "name") {
                                        licenses.add(child.textContent.trim())
                                    }
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }
                    if (licenses.isEmpty()) licenses.add("Unknown")

                    deps.add(mapOf(
                        "name" to dep.moduleName,
                        "group" to dep.moduleGroup,
                        "version" to dep.moduleVersion,
                        "licenses" to licenses.joinToString(", ")
                    ))
                }
            } catch (_: Exception) {
            }
        }

        val json = groovy.json.JsonOutput.toJson(deps.distinctBy { "${it["group"]}:${it["name"]}" }
            .sortedBy { it["name"].toString().lowercase() })

        outputFile.asFile.parentFile.mkdirs()
        outputFile.asFile.writeText(json)
    }
}

tasks.named("preBuild") {
    dependsOn(generateLicensesJson)
}
