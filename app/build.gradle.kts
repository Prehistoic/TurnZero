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
        val runtimeConfig = configurations.getByName("debugRuntimeClasspath")
        val resolved = runtimeConfig.resolvedConfiguration
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()

        try {
            resolved.resolvedArtifacts.forEach { artifact ->
                try {
                    val module = artifact.moduleVersion.id
                    val pomFile = artifact.file.resolveSibling(
                        "${module.name}-${module.version}.pom"
                    )
                    val licenses = mutableListOf<String>()
                    if (pomFile.exists()) {
                        val doc = factory.newDocumentBuilder().parse(pomFile)
                        val licenseNodes = doc.getElementsByTagName("license")
                        for (i in 0 until licenseNodes.length) {
                            val licenseNode = licenseNodes.item(i)
                            val nameNodes = licenseNode.childNodes
                            for (j in 0 until nameNodes.length) {
                                val child = nameNodes.item(j)
                                if (child.nodeName == "name") {
                                    licenses.add(child.textContent.trim())
                                }
                            }
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
                } catch (e: Exception) {
                    logger.warn("Failed to parse license: ${e.message}")
                }
            }
        } finally {
            runtimeConfig.resolvedConfiguration.rethrowFailure()
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
