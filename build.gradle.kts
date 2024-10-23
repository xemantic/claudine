plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.plugin.serialization)
  alias(libs.plugins.versions)
}

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
  }

//  macosArm64 {
//    binaries {
//      executable {
//        entryPoint = "com.xemantic.claudine.main"
//      }
//    }
//  }

  sourceSets {

    commonMain {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.anthropic.sdk.kotlin)
      }
    }

    val jvmAndPosix by creating {
      dependsOn(commonMain.get())
      dependencies {
        implementation(libs.kotlinx.serialization.json) // TODO is it runtimeOnly?

        implementation(libs.kotlinx.io)
        implementation(libs.kotlin.logging)
      }
    }

    jvmMain {
      dependsOn(jvmAndPosix)
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

    nativeMain {
      dependsOn(jvmAndPosix)
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
        "Main-Class" to "com.xemantic.claudine.ClaudineKt",
      )
    }
    from({
      main.runtimeDependencyFiles.files.filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
  }
}
