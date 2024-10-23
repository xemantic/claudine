package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.Text
import com.xemantic.anthropic.message.ToolResult
import java.io.File
import java.util.concurrent.TimeUnit

actual fun executeShell(
  toolUseId: String,
  command: String,
  workingDir: String,
  timeout: Int
): ToolResult = ProcessBuilder(
  listOf("sh", "-c", command)
)
  .directory(workingDir.sanitizeWorkDir())
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

private val userHomeDir = System.getProperty("user.home")!! // it must exist

private fun String.sanitizeWorkDir() = File(if (this == "~") userHomeDir else this)
