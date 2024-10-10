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
  .directory(File(if (workingDir == "~") System.getProperty("user.home")!! else workingDir))
  .redirectOutput(ProcessBuilder.Redirect.PIPE)
  .redirectError(ProcessBuilder.Redirect.PIPE)
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
