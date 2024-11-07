@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.plugin.serialization)
  alias(libs.plugins.versions)
}

val javaTarget = libs.versions.javaTarget.get()
val kotlinTarget = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())

repositories {
  mavenCentral()
  mavenLocal()
}

kotlin {

  applyDefaultHierarchyTemplate()

  jvm {
    // withJava()

//    mainRun {
//      mainClass = "com.xemantic.claudine.ClaudineKt"
//    }
    compilerOptions {
      apiVersion = kotlinTarget
      languageVersion = kotlinTarget
      jvmTarget = JvmTarget.fromTarget(javaTarget)
      freeCompilerArgs.add("-Xjdk-release=$javaTarget")
      progressiveMode = true
    }
  }

//  macosArm64 {
//    binaries {
//      executable {
//        entryPoint = "com.xemantic.claudine.main"
//      }
//    }
//  }


//  js {
//    browser()
//    nodejs()
//  }

  sourceSets {

    commonMain {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.anthropic.sdk.kotlin)
      }
    }

    val jvmAndPosixMain by creating {
      dependsOn(commonMain.get())
      dependencies {
        implementation(libs.kotlinx.serialization.json) // TODO is it runtimeOnly?

        implementation(libs.kotlinx.io)
        implementation(libs.kotlin.logging)
        implementation(libs.ktor.server.cio)
        implementation(libs.ktor.server.websockets)
      }
    }

    val jvmAndPosixTest by creating {
      dependsOn(commonTest.get())
      dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.kotlinx.io)
        implementation(libs.kotest.assertions.core)
      }
    }

    jvmMain {
      dependsOn(jvmAndPosixMain)
      dependencies {
        implementation(libs.ktor.client.java)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.client.logging)
        implementation(libs.ktor.serialization.kotlinx.json) // TODO is it runtimeOnly?
        implementation(libs.log4j.slf4j2)
        implementation(libs.log4j.core)
        implementation(libs.jackson.databind)
        implementation(libs.jackson.dataformat.yaml)
      }
    }

    jvmTest {
      dependsOn(jvmAndPosixTest)
    }

    nativeMain {
      dependsOn(jvmAndPosixMain)
    }

    nativeTest {
      dependsOn(jvmAndPosixTest)
    }

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

    jsMain {
      dependsOn(jvmAndPosixMain)
      dependencies {
//        implementation("org.jetbrains.kotlinx:kotlinx-nodejs:0.0.7")
      }
    }
  }

}

tasks.withType<JavaExec>().configureEach {
  standardInput = System.`in`
  jvmArgs = listOf("-Djava.net.preferIPv6Addresses=system")
}

tasks.withType<Jar> {
  doFirst {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val main by kotlin.jvm().compilations.getting
    manifest {
      attributes(
        "Main-Class" to "com.xemantic.claudine.ClaudineKt",
      )
    }
    from({
      main.runtimeDependencyFiles.files.filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
  }
}
