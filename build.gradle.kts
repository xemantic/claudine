@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.versions)
    alias(libs.plugins.kotlin.plugin.power.assert)
}

val buildNative: String? by project
val buildsNative: Boolean = (buildNative != null) && (buildNative!!.lowercase() == "true")

val javaTarget = libs.versions.javaTarget.get()
val kotlinTarget = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {

    compilerOptions {
        apiVersion = kotlinTarget
        languageVersion = kotlinTarget
        freeCompilerArgs.add("-Xmulti-dollar-interpolation")
        extraWarnings.set(true)
        progressiveMode = true
    }

    jvm {

        // set up according to https://jakewharton.com/gradle-toolchains-are-rarely-a-good-idea/
        compilerOptions {
            apiVersion = kotlinTarget
            languageVersion = kotlinTarget
            jvmTarget = JvmTarget.fromTarget(javaTarget)
            freeCompilerArgs.add("-Xjdk-release=$javaTarget")
            progressiveMode = true
        }

    }

    fun KotlinNativeTarget.claudineBinary() {
        binaries {
            executable {
                entryPoint = "com.xemantic.ai.claudine.main"
            }
        }
    }

    if (buildsNative) {

        linuxX64 {
            claudineBinary()
        }

        linuxArm64 {
            claudineBinary()
        }

        macosX64 {
            claudineBinary()
        }

        macosArm64 {
            claudineBinary()
        }

//        mingwX64 {
//            claudineBinary()
//        }

    }

    sourceSets {

        commonMain {
            dependencies {
                implementation(libs.anthropic.sdk.kotlin)
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotest.assertions.core)
                implementation(libs.xemantic.kotlin.test)
            }
        }

        jvmMain {
            dependencies {
                runtimeOnly(libs.ktor.client.java)

                runtimeOnly(libs.log4j.slf4j2)
                runtimeOnly(libs.log4j.core)
                runtimeOnly(libs.jackson.databind)
                runtimeOnly(libs.jackson.dataformat.yaml)
            }
        }

        if (buildsNative) {
            linuxMain {
                dependencies {
                    implementation(libs.ktor.client.curl)
                }
            }

            macosMain {
                dependencies {
                    implementation(libs.ktor.client.darwin)
                }
            }
        }

    }

}

tasks.withType<JavaExec>().configureEach {
    standardInput = System.`in`
}

tasks.withType<Jar> {
    doFirst {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        val main by kotlin.jvm().compilations.getting
        manifest {
            attributes(
                "Main-Class" to "com.xemantic.ai.claudine.ClaudineMain_jvmKt"
            )
        }
        from({
            main.runtimeDependencyFiles.files
                .filter { it.name.endsWith("jar") }
                .map { zipTree(it) }
        })
    }
}

if (buildsNative) {
    // most likely this is a problem only for mac
    // https://youtrack.jetbrains.com/issue/KT-64508/IndexOutOfBoundsException-in-Konan-StaticInitializersOptimization
    kotlin.targets.withType<KotlinNativeTarget> {
        binaries.all {
            freeCompilerArgs += "-Xdisable-phases=RemoveRedundantCallsToStaticInitializersPhase"
        }
    }
}

powerAssert {
    functions = listOf(
        "com.xemantic.kotlin.test.assert",
        "com.xemantic.kotlin.test.have"
    )
}