package com.xemantic.claudine.tool

import com.xemantic.anthropic.text.Text
import com.xemantic.anthropic.tool.ToolResult
import com.xemantic.claudine.system.operatingSystem
import java.io.File
import java.util.concurrent.TimeUnit

actual fun executeShell(
  toolUseId: String,
  command: String,
  workingDir: String,
  timeout: Int
): ToolResult = ProcessBuilder(getShellCommand() + command)
  .directory(File(workingDir.sanitizePath()))
  .redirectErrorStream(true)
  .redirectOutput(ProcessBuilder.Redirect.PIPE)
  .start().let {
    it.waitFor(timeout.toLong(), TimeUnit.SECONDS)

    ToolResult(
      toolUseId = toolUseId,
      content = listOf(
        Text(
          text = it.inputStream.bufferedReader().readText()
        )
      )
    )
  }

private fun getShellCommand() = if (
  operatingSystem.lowercase().contains("win")
) {
  listOf("powershell.exe", "-Command")
} else {
  listOf("sh", "-c")
}

private val userHomeDir = System.getProperty("user.home")!! // it must exist

private fun String.sanitizePath(): String =
  if (startsWith("~")) replace("~", userHomeDir) else this
