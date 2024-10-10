package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.tool.SerializableTool

@SerializableTool(
  name = "ExecuteShellCommand",
  description = "Executes shell command on human's machine. The timeout is defined in seconds"
)
data class ExecuteShellCommand(
  val command: String,
  val workingDir: String,
  val timeout: Int
) : SafeTool {

  override fun execute(toolUseId: String) = executeShell(
    toolUseId, command, workingDir, timeout
  )

}

expect fun executeShell(
  toolUseId: String,
  command: String,
  workingDir: String,
  timeout: Int
): ToolResult
