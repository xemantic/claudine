package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.Text
import com.xemantic.anthropic.message.ToolResult
import platform.posix.execlp

actual fun executeShell(
  toolUseId: String,
  command: String,
  workingDir: String,
  timeout: Int
): ToolResult {

  execlp(command, command)
  return ToolResult(
    toolUseId = toolUseId,
    content = listOf(
      Text(
        text = "foo"
      )
    )
  )
}
