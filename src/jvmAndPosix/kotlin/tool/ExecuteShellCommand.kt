package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.UsableTool

@AnthropicTool(
  name = "ExecuteShellCommand",
  description = """
## Execute shell command

Executes given shell command on human's machine.
 
The timeout is defined in seconds.

When using this command for listing files, try to minimize the amount of executions in favor of
recursive lists with depth of max 2 levels.
"""
)
data class ExecuteShellCommand(
  val command: String,
  val workingDir: String,
  val timeout: Int
) : UsableTool {

  override fun use(toolUseId: String) = executeShell(
    toolUseId, command, workingDir, timeout
  )

}

expect fun executeShell(
  toolUseId: String,
  command: String,
  workingDir: String,
  timeout: Int
): ToolResult
