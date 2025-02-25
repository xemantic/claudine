package com.xemantic.ai.claudine

import java.io.File
import java.util.concurrent.TimeUnit

actual val operatingSystem: String get() = System.getProperty("os.name")

actual fun ExecuteShellCommand.use(): String = ProcessBuilder(
  getShellCommand() + command
)
  .directory(File(workingDir.sanitizePath()))
  .redirectErrorStream(true)
  .redirectOutput(ProcessBuilder.Redirect.PIPE)
  .start().let {
    it.waitFor(timeout.toLong(), TimeUnit.SECONDS)
    it.inputStream.bufferedReader().readText()
  }


private val userHomeDir = System.getProperty("user.home")!! // it must exist

private fun String?.sanitizePath(): String =
  if (this == null) { "." }
  else if (startsWith("~")) replace("~", userHomeDir) else this
