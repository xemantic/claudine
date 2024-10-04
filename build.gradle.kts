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

  jvm()

  sourceSets {

    commonMain {
      dependencies {
        implementation(libs.anthropic.sdk.kotlin)
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.ktor.client.java)
      }
    }

  }

}
